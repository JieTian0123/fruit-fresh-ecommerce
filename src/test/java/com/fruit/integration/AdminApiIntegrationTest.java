package com.fruit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruit.utils.JwtUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Admin API集成测试 - 检测HTTP 200但业务码500的问题
 */
@SpringBootTest(properties = "spring.data.redis.repositories.enabled=false")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private ValueOperations<String, Object> valueOperations;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static final Long ADMIN_USER_ID = 1L;
    private static final Integer ADMIN_USER_TYPE = 2;
    private static final String TOKEN_PREFIX = "user:token:";

    // 记录所有失败的端点
    private static List<String> failedEndpoints = new ArrayList<>();
    private static List<String> successEndpoints = new ArrayList<>();

    @BeforeAll
    static void printHeader() {
        System.out.println("\n=== Admin API Integration Test ===");
    }

    @BeforeEach
    void setupToken() {
        // 为admin用户生成token，并用测试替身模拟Redis中的登录态
        adminToken = jwtUtils.generateToken(ADMIN_USER_ID, ADMIN_USER_TYPE);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(TOKEN_PREFIX + ADMIN_USER_ID)).thenReturn(adminToken);
    }

    @AfterAll
    static void printReport() {
        System.out.println("\n========================================");
        System.out.println("          API TEST REPORT");
        System.out.println("========================================");
        System.out.println("\n✅ SUCCESS (" + successEndpoints.size() + " endpoints):");
        successEndpoints.forEach(e -> System.out.println("   " + e));

        System.out.println("\n❌ FAILED (code=500) (" + failedEndpoints.size() + " endpoints):");
        failedEndpoints.forEach(e -> System.out.println("   " + e));

        System.out.println("\n========================================");
        System.out.println("Total: " + (successEndpoints.size() + failedEndpoints.size()) +
                          " | Success: " + successEndpoints.size() +
                          " | Failed: " + failedEndpoints.size());
        System.out.println("========================================\n");
    }

    private void testEndpoint(String method, String url, String body) throws Exception {
        MvcResult result;
        String endpoint = method + " " + url;

        try {
            switch (method) {
                case "GET":
                    result = mockMvc.perform(get(url)
                            .header("Authorization", "Bearer " + adminToken))
                            .andExpect(status().isOk())
                            .andReturn();
                    break;
                case "POST":
                    result = mockMvc.perform(post(url)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body != null ? body : "{}"))
                            .andExpect(status().isOk())
                            .andReturn();
                    break;
                case "PUT":
                    result = mockMvc.perform(put(url)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body != null ? body : "{}"))
                            .andExpect(status().isOk())
                            .andReturn();
                    break;
                case "DELETE":
                    result = mockMvc.perform(delete(url)
                            .header("Authorization", "Bearer " + adminToken))
                            .andExpect(status().isOk())
                            .andReturn();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown method: " + method);
            }

            String responseBody = result.getResponse().getContentAsString();
            JsonNode json = objectMapper.readTree(responseBody);
            int code = json.path("code").asInt();
            String message = json.path("message").asText();

            if (code == 200) {
                successEndpoints.add(endpoint);
                System.out.println("✅ " + endpoint + " -> code=" + code);
            } else {
                failedEndpoints.add(endpoint + " -> code=" + code + ", msg=" + message);
                System.out.println("❌ " + endpoint + " -> code=" + code + ", msg=" + message);
                System.out.println("   Full response: " + responseBody);
            }
        } catch (Exception e) {
            failedEndpoints.add(endpoint + " -> EXCEPTION: " + e.getMessage());
            System.out.println("💥 " + endpoint + " -> EXCEPTION: " + e.getMessage());
        }
    }

    // ========== AdminStatsController ==========
    @Test
    @Order(1)
    void testAdminStatsOverview() throws Exception {
        testEndpoint("GET", "/admin/stats/overview", null);
    }

    @Test
    @Order(2)
    void testAdminStatsGrowth() throws Exception {
        testEndpoint("GET", "/admin/stats/growth", null);
    }

    @Test
    @Order(3)
    void testAdminStatsPeriod() throws Exception {
        testEndpoint("GET", "/admin/stats/period?period=week", null);
    }

    // ========== VipManageController ==========
    @Test
    @Order(10)
    void testVipPlans() throws Exception {
        testEndpoint("GET", "/admin/vip/plans", null);
    }

    @Test
    @Order(11)
    void testVipUsers() throws Exception {
        testEndpoint("GET", "/admin/vip/users", null);
    }

    @Test
    @Order(12)
    void testVipOrders() throws Exception {
        testEndpoint("GET", "/admin/vip/orders", null);
    }

    // ========== AnnouncementManageController ==========
    @Test
    @Order(20)
    void testAnnouncementList() throws Exception {
        testEndpoint("GET", "/admin/announcement/list", null);
    }

    @Test
    @Order(21)
    void testAnnouncementDetail() throws Exception {
        testEndpoint("GET", "/admin/announcement/detail/1", null);
    }

    // ========== CouponManageController ==========
    @Test
    @Order(30)
    void testCouponList() throws Exception {
        testEndpoint("GET", "/admin/coupon/list", null);
    }

    @Test
    @Order(31)
    void testCouponDetail() throws Exception {
        testEndpoint("GET", "/admin/coupon/detail/1", null);
    }

    // ========== ShopManageController ==========
    @Test
    @Order(40)
    void testShopList() throws Exception {
        testEndpoint("GET", "/admin/shop/list", null);
    }

    @Test
    @Order(41)
    void testShopDetail() throws Exception {
        testEndpoint("GET", "/admin/shop/detail/1", null);
    }

    // ========== OrderManageController ==========
    @Test
    @Order(50)
    void testOrderList() throws Exception {
        testEndpoint("GET", "/admin/order/list", null);
    }

    // ========== BannerManageController ==========
    @Test
    @Order(60)
    void testBannerList() throws Exception {
        testEndpoint("GET", "/admin/banner/list", null);
    }

    @Test
    @Order(61)
    void testBannerDetail() throws Exception {
        testEndpoint("GET", "/admin/banner/detail/1", null);
    }

    // ========== UserManageController ==========
    @Test
    @Order(70)
    void testUserList() throws Exception {
        testEndpoint("GET", "/admin/user/list", null);
    }

    @Test
    @Order(71)
    void testUserDetail() throws Exception {
        testEndpoint("GET", "/admin/user/detail/1", null);
    }

    // ========== ProductManageController ==========
    @Test
    @Order(80)
    void testProductList() throws Exception {
        testEndpoint("GET", "/admin/product/list", null);
    }

    @Test
    @Order(81)
    void testProductDetail() throws Exception {
        testEndpoint("GET", "/admin/product/detail/1", null);
    }

    // ========== CategoryManageController ==========
    @Test
    @Order(90)
    void testCategoryList() throws Exception {
        testEndpoint("GET", "/admin/category/list", null);
    }

    @Test
    @Order(91)
    void testCategoryTree() throws Exception {
        testEndpoint("GET", "/admin/category/tree", null);
    }

    @Test
    @Order(92)
    void testCategoryDetail() throws Exception {
        testEndpoint("GET", "/admin/category/detail/1", null);
    }

    // ========== POST/PUT/DELETE Tests ==========

    // VIP Plan CRUD
    @Test
    @Order(100)
    void testVipPlanAdd() throws Exception {
        String body = "{\"name\":\"测试套餐\",\"price\":9.9,\"durationDays\":30,\"description\":\"测试\",\"benefits\":\"测试权益\",\"sort\":99,\"status\":0}";
        testEndpoint("POST", "/admin/vip/plan", body);
    }

    @Test
    @Order(101)
    void testVipPlanUpdate() throws Exception {
        String body = "{\"id\":4,\"name\":\"年度会员更新\",\"price\":68.0,\"durationDays\":365,\"description\":\"更新描述\",\"benefits\":\"更新权益\",\"sort\":1,\"status\":1}";
        testEndpoint("PUT", "/admin/vip/plan", body);
    }

    @Test
    @Order(102)
    void testVipPlanStatusUpdate() throws Exception {
        testEndpoint("PUT", "/admin/vip/plan/4/status?status=1", null);
    }

    // Announcement CRUD
    @Test
    @Order(110)
    void testAnnouncementAdd() throws Exception {
        String body = "{\"type\":1,\"title\":\"测试公告\",\"content\":\"测试内容\",\"status\":0,\"sort\":0}";
        testEndpoint("POST", "/admin/announcement/add", body);
    }

    @Test
    @Order(111)
    void testAnnouncementUpdate() throws Exception {
        String body = "{\"id\":1,\"type\":1,\"title\":\"欢迎来到水果生鲜电商平台\",\"content\":\"更新内容\",\"status\":1}";
        testEndpoint("PUT", "/admin/announcement/update", body);
    }

    @Test
    @Order(112)
    void testAnnouncementPublish() throws Exception {
        testEndpoint("POST", "/admin/announcement/publish/1", null);
    }

    @Test
    @Order(113)
    void testAnnouncementUnpublish() throws Exception {
        testEndpoint("POST", "/admin/announcement/unpublish/1", null);
    }

    // Coupon CRUD
    @Test
    @Order(120)
    void testCouponAdd() throws Exception {
        String body = "{\"title\":\"测试优惠券\",\"couponType\":1,\"discountAmount\":5.0,\"minimumAmount\":10.0,\"totalQuantity\":100,\"perUserLimit\":1,\"validFrom\":\"2026-04-01 00:00:00\",\"validUntil\":\"2026-12-31 23:59:59\",\"status\":0}";
        testEndpoint("POST", "/admin/coupon/add", body);
    }

    @Test
    @Order(121)
    void testCouponUpdate() throws Exception {
        String body = "{\"id\":1,\"title\":\"新用户专享券更新\",\"couponType\":3,\"discountAmount\":10.0,\"totalQuantity\":10000,\"perUserLimit\":1,\"status\":1}";
        testEndpoint("PUT", "/admin/coupon/update", body);
    }

    @Test
    @Order(122)
    void testCouponStatusUpdate() throws Exception {
        testEndpoint("PUT", "/admin/coupon/status/1?status=1", null);
    }

    // Banner CRUD
    @Test
    @Order(130)
    void testBannerAdd() throws Exception {
        String body = "{\"title\":\"测试轮播图\",\"imageUrl\":\"https://test.com/image.jpg\",\"linkUrl\":\"/product/1\",\"linkType\":1,\"sort\":99,\"status\":0}";
        testEndpoint("POST", "/admin/banner/add", body);
    }

    @Test
    @Order(131)
    void testBannerUpdate() throws Exception {
        String body = "{\"title\":\"更新轮播图\",\"imageUrl\":\"https://test.com/updated.jpg\",\"linkUrl\":\"/product/2\",\"linkType\":1,\"sort\":1,\"status\":1}";
        testEndpoint("PUT", "/admin/banner/update/1", body);
    }

    @Test
    @Order(132)
    void testBannerStatusUpdate() throws Exception {
        testEndpoint("PUT", "/admin/banner/status/1?status=1", null);
    }

    // Category CRUD
    @Test
    @Order(140)
    void testCategoryAdd() throws Exception {
        String body = "{\"name\":\"测试分类\",\"parentId\":0,\"icon\":\"test-icon\",\"sort\":99,\"status\":1}";
        testEndpoint("POST", "/admin/category/add", body);
    }

    @Test
    @Order(141)
    void testCategoryUpdate() throws Exception {
        String body = "{\"name\":\"水果更新\",\"parentId\":0,\"icon\":\"updated-icon\",\"sort\":1,\"status\":1}";
        testEndpoint("PUT", "/admin/category/update/1", body);
    }

    @Test
    @Order(142)
    void testCategoryStatusUpdate() throws Exception {
        testEndpoint("PUT", "/admin/category/status/1?status=1", null);
    }

    // User Management
    @Test
    @Order(150)
    void testUserApprove() throws Exception {
        testEndpoint("PUT", "/admin/user/approve/2?status=1", null);
    }

    @Test
    @Order(151)
    void testUserDisable() throws Exception {
        testEndpoint("PUT", "/admin/user/disable/99", null);  // Non-existent user to avoid side effects
    }

    @Test
    @Order(152)
    void testUserEnable() throws Exception {
        testEndpoint("PUT", "/admin/user/enable/2", null);
    }

    // Shop Management
    @Test
    @Order(160)
    void testShopApprove() throws Exception {
        testEndpoint("PUT", "/admin/shop/approve/1?status=1", null);
    }

    // Product Management
    @Test
    @Order(170)
    void testProductApprove() throws Exception {
        testEndpoint("PUT", "/admin/product/approve/1?status=1", null);
    }

    // Order detail by orderNo
    @Test
    @Order(180)
    void testOrderDetail() throws Exception {
        testEndpoint("GET", "/admin/order/detail/20260404165035314092", null);
    }
}
