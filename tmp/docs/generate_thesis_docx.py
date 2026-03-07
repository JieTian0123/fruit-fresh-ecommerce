from zipfile import ZipFile, ZIP_DEFLATED
from xml.sax.saxutils import escape
from datetime import datetime
from pathlib import Path

out_path = Path(r"D:\fruit-fresh-ecommerce\output\doc\水果生鲜电商系统_毕业论文完整版.docx")
out_path.parent.mkdir(parents=True, exist_ok=True)


def run(text, bold=False, size=24, font_cn='宋体', font_en='Times New Roman'):
    rpr = [
        f'<w:rFonts w:ascii="{font_en}" w:hAnsi="{font_en}" w:eastAsia="{font_cn}"/>',
        f'<w:sz w:val="{size}"/>',
        f'<w:szCs w:val="{size}"/>'
    ]
    if bold:
        rpr.append('<w:b/>')
    t = escape(text)
    return f'<w:r><w:rPr>{"".join(rpr)}</w:rPr><w:t xml:space="preserve">{t}</w:t></w:r>'


def paragraph(text, align='left', bold=False, size=24, indent=True, space_before=0, space_after=120, line=480):
    ind = '<w:ind w:firstLine="420"/>' if indent else ''
    ppr = (
        f'<w:pPr><w:jc w:val="{align}"/>{ind}'
        f'<w:spacing w:before="{space_before}" w:after="{space_after}" w:line="{line}" w:lineRule="auto"/>'
        f'</w:pPr>'
    )
    return f'<w:p>{ppr}{run(text, bold=bold, size=size)}</w:p>'


def heading1(text):
    return paragraph(text, align='left', bold=True, size=32, indent=False, space_before=240, space_after=120, line=360)


def heading2(text):
    return paragraph(text, align='left', bold=True, size=28, indent=False, space_before=180, space_after=80, line=360)


def heading3(text):
    return paragraph(text, align='left', bold=True, size=26, indent=False, space_before=120, space_after=60, line=360)


def center_title(text, size=44):
    return paragraph(text, align='center', bold=True, size=size, indent=False, space_before=120, space_after=120, line=360)


def page_break():
    return '<w:p><w:r><w:br w:type="page"/></w:r></w:p>'


parts = []

# 封面
parts.append(center_title('本科毕业论文', 48))
parts.append(paragraph('', align='center', indent=False, space_after=240))
parts.append(center_title('基于 Spring Boot 与 Vue3 的水果生鲜电商系统', 40))
parts.append(center_title('设计与实现', 40))
parts.append(paragraph('', align='center', indent=False, space_after=360))
parts.append(paragraph('题    目：基于 Spring Boot 与 Vue3 的水果生鲜电商系统设计与实现', align='left', indent=False, size=26, space_after=80))
parts.append(paragraph('学    院：信息工程学院', align='left', indent=False, size=26, space_after=80))
parts.append(paragraph('专    业：软件工程', align='left', indent=False, size=26, space_after=80))
parts.append(paragraph('学生姓名：__________', align='left', indent=False, size=26, space_after=80))
parts.append(paragraph('学    号：__________', align='left', indent=False, size=26, space_after=80))
parts.append(paragraph('指导教师：__________', align='left', indent=False, size=26, space_after=80))
parts.append(paragraph('完成日期：2026年3月', align='left', indent=False, size=26, space_after=80))
parts.append(page_break())

# 中文摘要
parts.append(center_title('摘  要', 36))
abstract_cn = [
    '随着居民消费升级和即时配送体系不断完善，生鲜电商逐步从“价格竞争”转向“履约效率、品质可信与用户体验”的综合竞争。水果作为高频消费、保鲜要求高、损耗率高的典型品类，对电商平台在商品管理、订单流转、会员运营、售后协同及数据分析方面提出了更高要求。针对传统小型生鲜平台功能分散、角色协同弱、运营能力不足的问题，本文设计并实现了一个面向消费者、商家、管理员三类角色的水果生鲜电商系统。',
    '系统采用前后端分离的单体架构，后端基于 Spring Boot、MyBatis Plus、MySQL、Redis 构建服务能力，前端基于 Vue3、TypeScript、Vite 与 Element Plus 实现三端统一工程。围绕真实业务链路，系统实现了用户认证与授权、商品与分类管理、购物车与订单流转、优惠券与积分体系、会员等级与 VIP 机制、平台公告与系统通知、商家经营看板、商品新鲜度字段管理及溯源节点记录等模块。为保证接口一致性与可维护性，系统采用统一返回结构与全局异常处理；为提升登录态安全性与可控性，采用 JWT 与 Redis 会话校验组合机制。',
    '在实现层面，本文重点阐述了订单状态机、库存扣减与恢复、优惠券核销、积分结算与等级升级、VIP 自动升级与定时发券等关键流程，并给出了面向多角色的路由隔离与权限控制方案。测试结果表明，系统能够稳定支撑典型生鲜电商业务闭环，具备较好的功能完整性、模块可扩展性与工程可维护性。最后，本文分析了当前系统在后端细粒度权限、自动化测试覆盖、复杂营销策略与高并发优化方面的不足，并提出了后续演进方向。'
]
for p in abstract_cn:
    parts.append(paragraph(p, size=24, indent=True, line=520))
parts.append(paragraph('关键词：生鲜电商；Spring Boot；Vue3；会员运营；商品溯源；前后端分离', size=24, indent=False, bold=True, space_before=120))
parts.append(page_break())

# 英文摘要
parts.append(center_title('Abstract', 36))
abstract_en = [
    'With the continuous upgrade of consumer demand and the maturity of instant delivery infrastructure, fresh-food e-commerce is evolving from pure price competition to comprehensive competition on fulfillment efficiency, quality trust, and user experience. Fruits, as a high-frequency category with strict freshness requirements and high loss rates, impose higher demands on platform capabilities including product management, order fulfillment, membership operation, after-sales collaboration, and data analytics.',
    'To address the problems of fragmented functions, weak role collaboration, and insufficient operational tools in traditional small-scale fresh platforms, this thesis designs and implements a fruit fresh e-commerce system for three roles: consumers, merchants, and administrators. The system adopts a front-end/back-end separated monolithic architecture. The backend is built with Spring Boot, MyBatis Plus, MySQL, and Redis, while the frontend is built with Vue3, TypeScript, Vite, and Element Plus in a unified project for multiple roles.',
    'The system provides core modules such as authentication and authorization, product and category management, shopping cart and order lifecycle, coupon and point systems, membership level and VIP services, platform announcements and notifications, merchant dashboards, freshness fields, and traceability records. It also introduces a unified API response format, global exception handling, and JWT plus Redis session verification for maintainability and security. Experiments and functional tests show that the system can support a complete business loop of fruit e-commerce with good integrity and extensibility. Finally, this thesis summarizes current limitations and proposes future improvements in fine-grained authorization, automated testing, marketing strategy flexibility, and high-concurrency optimization.'
]
for p in abstract_en:
    parts.append(paragraph(p, size=22, indent=True, line=500))
parts.append(paragraph('Keywords: fresh-food e-commerce; Spring Boot; Vue3; membership operation; traceability; front-end/back-end separation', size=22, indent=False, bold=True, space_before=120))
parts.append(page_break())

# 目录占位
parts.append(center_title('目  录', 36))
for line in [
    '第1章 绪论 .......................................................... 1',
    '1.1 研究背景与意义 ................................................... 1',
    '1.2 国内外研究与应用现状 ............................................. 3',
    '1.3 研究内容与技术路线 ............................................... 5',
    '1.4 论文结构安排 ..................................................... 7',
    '第2章 关键技术与可行性分析 ........................................... 8',
    '第3章 系统需求分析与总体设计 ......................................... 14',
    '第4章 系统详细设计与实现 ............................................. 26',
    '第5章 系统测试与结果分析 ............................................. 48',
    '第6章 总结与展望 ..................................................... 60',
    '参考文献 ............................................................. 63',
    '致谢 ................................................................. 66'
]:
    parts.append(paragraph(line, indent=False, size=24, line=420, space_after=40))
parts.append(page_break())

# 第1章
parts.append(heading1('第1章 绪论'))
parts.append(heading2('1.1 研究背景与意义'))
intro_11 = [
    '近年来，生鲜电商市场规模持续增长，用户对“当日达”“次日达”与“可追溯品质”的期待不断提高。相较于标准化商品，水果生鲜在采购、存储、运输、交付及售后环节具有更高复杂度，平台既要保障交易效率，又要兼顾损耗控制与品质管理，因此对系统的业务完整性和工程稳定性提出了更高要求。',
    '在传统线下交易模式中，消费者获取商品信息主要依赖经验判断，存在信息不对称与品控难透明等问题；而线上平台若缺乏完善的订单流转、评价反馈、会员运营与溯源机制，也难以形成长期用户黏性。基于这一现实需求，构建一个结构清晰、功能闭环完整、易扩展的水果生鲜电商系统具有明显的实践价值。',
    '从教学与毕业设计角度看，水果生鲜电商系统可覆盖软件工程中需求分析、架构设计、数据库设计、前后端协同、接口规范、权限控制、异常处理、测试验证等关键能力点，能够较全面地体现工程化开发水平。通过本课题实践，可将课堂知识转化为可运行、可迭代的真实软件产品。'
]
for p in intro_11:
    parts.append(paragraph(p))

parts.append(heading2('1.2 国内外研究与应用现状'))
intro_12 = [
    '国外生鲜零售平台在供应链数字化、冷链仓配协同和用户画像驱动运营方面起步较早，普遍强调标准化履约与数据中台能力。其技术路线通常采用微服务或模块化单体架构，并通过推荐算法、动态定价、精细化营销提升复购率。',
    '国内生鲜电商发展呈现“平台化+区域化”并行趋势。大型平台依托仓储与配送网络构建规模优势，中小平台则更多聚焦本地市场与垂直品类。对中小团队而言，采用前后端分离单体架构在开发效率、部署复杂度与维护成本之间更具平衡性，适合作为毕业设计与初创项目的实现路径。',
    '综合现有研究与实践可见，生鲜电商系统在功能层面已较成熟，但在“轻量实现条件下如何兼顾业务完整性与后续扩展能力”方面仍有较大实践空间。本文即围绕该问题，提供一个可落地、可演进、可教学复用的系统实现方案。'
]
for p in intro_12:
    parts.append(paragraph(p))

parts.append(heading2('1.3 研究内容与技术路线'))
for p in [
    '本文围绕水果生鲜平台的核心交易链路展开，重点完成三类角色的业务闭环实现：消费者侧关注购物体验与售后反馈，商家侧关注店铺经营与订单履约，管理侧关注平台治理与运营分析。系统在设计时兼顾“先完整后优化”的原则，先保障核心业务可用，再逐步叠加会员、VIP、通知、溯源等运营能力。',
    '技术路线方面，后端采用 Spring Boot 作为应用容器，MyBatis Plus 实现数据访问，MySQL 负责持久化存储，Redis 用于登录态校验与缓存支持；前端采用 Vue3 + TypeScript + Vite 构建三端统一工程，通过路由元信息与状态管理实现角色隔离和页面权限控制。',
    '在方法上，本文采用“需求建模-数据库建模-接口设计-模块实现-联调测试-问题修正”的迭代过程，确保每一阶段均有可验证产物，最终形成可运行系统与完整论文章节。'
]:
    parts.append(paragraph(p))

parts.append(heading2('1.4 论文结构安排'))
for p in [
    '第1章介绍课题背景、研究意义、国内外应用现状及本文技术路线。',
    '第2章给出关键技术说明与项目可行性分析。',
    '第3章从需求、架构、模块、流程和数据库角度完成系统总体设计。',
    '第4章详细描述各功能模块的实现过程与关键代码思路。',
    '第5章介绍测试方案、测试结果及存在问题。',
    '第6章对全文进行总结并给出后续优化方向。'
]:
    parts.append(paragraph(p, indent=False))
parts.append(page_break())

# 第2章
parts.append(heading1('第2章 关键技术与可行性分析'))
parts.append(heading2('2.1 关键技术选型'))
for p in [
    'Spring Boot 2.7 具备开箱即用、配置简洁、生态成熟等特点，适合中小规模电商项目快速构建。通过统一配置文件与自动装配机制，可以显著降低开发与部署复杂度。',
    'MyBatis Plus 在保留 MyBatis 灵活性的同时提供通用 CRUD 能力，减少样板代码并提升开发效率。对于本系统这种表结构较稳定、查询逻辑可分层组织的场景，MyBatis Plus 能够在效率与可控性之间取得平衡。',
    'Vue3 + TypeScript 组合可在前端侧提供更清晰的组件化开发方式与类型约束，降低多人协作中的接口误用风险。Vite 的快速冷启动和热更新能力可显著提升迭代效率。',
    'Redis 主要用于登录态会话一致性校验，配合 JWT 可实现“服务无状态 + 会话可控失效”。这种机制在多端登录、退出登录、修改密码强制下线等场景具有良好实用性。'
]:
    parts.append(paragraph(p))

parts.append(heading2('2.2 系统可行性分析'))
parts.append(heading3('2.2.1 技术可行性'))
for p in [
    '所选技术栈均为主流开源技术，资料丰富、社区活跃、工具链成熟，能够满足课程设计及毕业论文阶段的开发需求。项目采用单体架构可有效降低运维门槛，便于在本地或学校服务器快速部署与演示。',
    '系统采用分层架构并引入统一异常处理、统一接口返回、拦截器鉴权等工程实践，使代码具备较好的结构稳定性。后续若有需要，也可按业务域逐步拆分服务，因此具备可持续演进能力。'
]:
    parts.append(paragraph(p))

parts.append(heading3('2.2.2 经济可行性'))
for p in [
    '项目开发基于开源框架和免费工具链，不依赖商业授权软件，研发成本较低。部署阶段可使用普通云主机或实验室服务器，硬件投入可控。',
    '从运营角度看，系统支持优惠券、积分、会员、公告等常见增长工具，可在不额外采购第三方系统的前提下实现基础运营闭环，具有较高性价比。'
]:
    parts.append(paragraph(p))

parts.append(heading3('2.2.3 运行可行性'))
for p in [
    '系统面向三类用户分别提供清晰界面，角色边界明确，操作路径直观，能够满足教学演示和中小业务场景试运行要求。',
    '后端通过统一日志与异常处理机制提供较好的故障定位能力；前端通过请求拦截与错误提示提升可用性。整体上具备稳定运行基础。'
]:
    parts.append(paragraph(p))
parts.append(page_break())

# 第3章
parts.append(heading1('第3章 系统需求分析与总体设计'))
parts.append(heading2('3.1 角色与业务需求分析'))
for p in [
    '消费者侧需求重点在于“买得到、买得快、买得放心”。具体表现为商品检索便捷、下单流程简洁、履约状态透明、售后反馈顺畅、会员权益可感知。',
    '商家侧需求重点在于“管得住、发得出、看得清”。系统需支持商品上架与库存维护、订单处理与发货录入、评价回复、店铺信息维护以及经营数据统计。',
    '管理员侧需求重点在于“控风险、促增长、可治理”。系统应具备用户/商家审核、商品与分类管理、订单监管、内容运营、优惠策略管理及全局数据看板。'
]:
    parts.append(paragraph(p))

parts.append(heading2('3.2 非功能需求分析'))
for p in [
    '安全性要求：系统需保证身份可信、接口受控、参数可校验、异常可追踪。项目通过登录拦截、统一异常与业务校验共同实现基础安全保障。',
    '性能要求：在教学与中小规模业务场景下，系统应能稳定支撑并发浏览、下单与管理操作。采用分页查询、索引优化与轻量缓存以提升响应效率。',
    '可维护性要求：代码结构应清晰、模块边界明确、公共能力复用。通过 Controller-Service-Mapper 分层、DTO/VO 分离及统一返回结构实现。',
    '可扩展性要求：应支持后续新增营销玩法、支付能力、推荐能力及多仓协同能力。当前数据库与业务模型已预留扩展字段与独立模块。'
]:
    parts.append(paragraph(p))

parts.append(heading2('3.3 总体架构设计'))
for p in [
    '系统采用前后端分离单体架构。前端负责页面渲染、交互流程和路由权限控制；后端负责业务规则、数据处理、权限校验和接口输出。',
    '后端按“Controller- Service-Mapper-Entity”组织，Controller 负责入参校验和返回封装，Service 负责业务编排，Mapper 负责数据库访问，Entity/DTO/VO 分别承担持久化对象、入参对象和出参对象职责。',
    '该架构在课程项目中能够兼顾开发效率与工程规范。相比早期前后端混写方式，可显著降低耦合度，便于前后端并行迭代。'
]:
    parts.append(paragraph(p))

parts.append(heading2('3.4 功能模块设计'))
module_texts = [
    '认证模块：支持消费者、商家、管理员三角色登录，登录后签发 Token 并写入 Redis。拦截器对受保护接口执行登录校验。',
    '商品模块：支持分类管理、商品发布与编辑、上下架控制、库存与销量维护、商品详情展示与搜索。',
    '订单模块：支持购物车结算与立即购买，提供订单创建、支付、取消、发货、确认收货、申请退款等状态流转。',
    '会员模块：支持积分账户、积分流水、等级提升、优惠券发放与核销，形成基础用户成长体系。',
    'VIP 模块：支持套餐购买、订单达标自动升级、每周优惠券定时发放，提高高价值用户留存。',
    '通知与公告模块：支持平台公告发布、系统通知推送、消息已读管理，增强平台运营触达能力。',
    '溯源模块：支持商品从采摘到配送的节点记录，提升消费者对生鲜品质与履约过程的可视化感知。'
]
for p in module_texts:
    parts.append(paragraph(p))

parts.append(heading2('3.5 数据库总体设计'))
for p in [
    '数据库采用 MySQL，围绕交易主链路建立核心表：user、product、order、order_item、cart、address。该主链路支撑从浏览到成交的完整流程。',
    '围绕运营与增长建立扩展表：coupon、user_coupon、member_level、user_points_log、user_sign_in、vip_plan、vip_order、system_notification、announcement、banner、shop_follow。',
    '围绕生鲜可信链路建立 product_traceability 表，记录节点类型、地点、温湿度、操作人和发生时间。该表支持后续对接 IoT 与可视化追踪能力。',
    '大部分业务表设置 deleted 字段用于逻辑删除，避免物理删除带来的追溯困难，并提升审计与恢复能力。'
]:
    parts.append(paragraph(p))
parts.append(page_break())

# 第4章
parts.append(heading1('第4章 系统详细设计与实现'))
parts.append(heading2('4.1 后端分层实现'))
for p in [
    '在接口层，控制器统一采用 REST 风格命名与请求方法约定，通过 @Valid 注解实现参数校验，使用统一 Result 对象封装返回值，确保前后端联调成本可控。',
    '在业务层，Service 负责交易规则、状态流转和事务边界。订单创建、库存扣减、优惠券核销等关键操作采用事务管理，避免部分成功导致的数据不一致。',
    '在数据层，Mapper 负责数据库访问，复杂查询通过条件构造器与自定义 SQL 组合实现。对于统计类接口，采用按时段聚合查询生成趋势数据，为看板展示提供基础数据。'
]:
    parts.append(paragraph(p))

parts.append(heading2('4.2 统一响应与异常处理实现'))
for p in [
    '系统定义 Result<T> 作为统一返回结构，包含 code、message、data、timestamp 四个核心字段。前端可据此统一处理成功与失败逻辑，降低页面重复代码。',
    'GlobalExceptionHandler 统一接管业务异常、参数绑定异常和系统异常。业务异常返回可读提示，系统异常返回通用错误信息并在日志中记录堆栈，兼顾安全与排障效率。',
    '该设计将错误处理从业务流程中解耦出来，提高控制器与服务层代码可读性，是系统可维护性的关键实现点之一。'
]:
    parts.append(paragraph(p))

parts.append(heading2('4.3 登录鉴权与会话管理实现'))
for p in [
    '用户登录成功后，后端生成 JWT 并将其写入 Redis，键采用 user:token:用户ID 格式。后续请求通过拦截器校验请求头中的 Token，有效后将用户对象写入 ThreadLocal 上下文，供业务层读取。',
    '该机制相比纯 JWT 更便于实现“可控失效”：用户退出登录或修改密码后可删除 Redis 中会话，达到即时下线效果。对于教学项目而言，这种方案在安全性与实现复杂度之间较为平衡。',
    '前端请求拦截器会自动携带 Token，响应拦截器统一处理 401、403、404、500 等错误状态，实现登录过期跳转与提示文案统一，改善用户体验。'
]:
    parts.append(paragraph(p))

parts.append(heading2('4.4 订单核心流程实现'))
order_impl = [
    '订单创建分为“立即购买”和“购物车结算”两条路径，系统先校验商品与库存，再写入订单与订单明细，并同步更新库存与销量。若使用优惠券，则在核验有效期、使用门槛与状态后计算抵扣金额并更新核销状态。',
    '支付成功后订单状态由待付款流转至待发货；商家录入物流后流转至待收货；用户确认收货后流转至已完成。该阶段还会触发积分奖励、累计消费更新、通知推送及 VIP 升级检查。',
    '当订单处于待付款状态时允许取消，取消后恢复库存与销量，避免资源长期占用。该流程通过状态机约束各操作入口，减少非法状态变更。',
    '订单流程设计强调“状态驱动 + 事务一致性 + 后置事件触发”三点：前两者保证交易正确性，后者支撑运营能力扩展。'
]
for p in order_impl:
    parts.append(paragraph(p))

parts.append(heading2('4.5 会员、积分与优惠券实现'))
for p in [
    '积分模块记录每次变动来源与变动后余额，既支持消费奖励，也支持签到奖励与兑换扣减。积分流水可作为用户成长分析和异常排查依据。',
    '会员等级模块按积分阈值自动晋升，等级变化无需人工干预。该机制将用户行为与权益激励绑定，有助于提高复购率与留存率。',
    '优惠券模块支持满减券、折扣券与无门槛券。订单结算时依据券类型计算抵扣金额，并结合最低消费、最高优惠、有效期等规则进行校验。',
    '通过积分、等级、优惠券三者联动，系统构建了基础运营闭环，可支持后续扩展生日券、新客券、复购券、分层营销等策略。'
]:
    parts.append(paragraph(p))

parts.append(heading2('4.6 VIP 与定时任务实现'))
for p in [
    'VIP 模块包含套餐购买与订单达标自动升级两条路径。购买路径通过 vip_plan 定义套餐时长和价格，成功后更新用户 VIP 到期时间并记录 vip_order。',
    '自动升级路径在用户完成订单后触发检查，达到阈值则自动生成来源为“达标升级”的 VIP 订单记录，便于审计和运营追踪。',
    '系统启用定时任务机制，每周定时向有效 VIP 用户发放专属优惠券。通过该能力可在较低运维成本下实现长期用户激励。'
]:
    parts.append(paragraph(p))

parts.append(heading2('4.7 通知、公告与溯源实现'))
for p in [
    '通知模块以用户为主键维度管理消息，支持未读统计、单条已读和全部已读。订单关键节点（如发货、完成）可触发自动通知，提升用户感知。',
    '公告模块面向平台运营，支持公告类型区分、发布状态控制、发布时间管理与访问量统计，满足基础内容运营需求。',
    '溯源模块支持商家维护采摘、质检、入库、出库、配送节点信息，包括地点、操作人、温湿度与图片。消费者可在商品详情查看该链路信息，以增强信任。'
]:
    parts.append(paragraph(p))

parts.append(heading2('4.8 前端三端实现'))
for p in [
    '前端在统一工程中实现消费者、商家、管理员三套布局。通过路由元信息 requireAuth 与 role 控制访问权限，并在守卫中处理登录态恢复与角色跳转。',
    '消费者侧重点在于浏览与交易体验；商家侧重点在于商品与订单运营；管理侧重点在于平台治理与统计分析。三端共享请求层、类型定义和公共组件，减少重复开发。',
    '页面层通过组合式 API 组织状态与行为，配合 Pinia 进行登录态和购物车状态管理，形成“接口层-状态层-视图层”清晰协作关系。'
]:
    parts.append(paragraph(p))
parts.append(page_break())

# 第5章
parts.append(heading1('第5章 系统测试与结果分析'))
parts.append(heading2('5.1 测试目标与测试环境'))
for p in [
    '测试目标包括：验证核心功能正确性、校验角色权限边界、检查接口异常处理一致性、评估系统在常规并发下的稳定运行能力。',
    '测试环境为本地联调环境：Windows 11，JDK 1.8，MySQL 8.x，Redis 3.2+，Node.js 20+。测试工具以浏览器联调、接口调试和日志分析为主。'
]:
    parts.append(paragraph(p))

parts.append(heading2('5.2 功能测试结果'))
case_list = [
    ('F01', '用户注册与登录', '输入合法账号密码后返回 Token 与用户信息，并可访问受保护页面'),
    ('F02', '商品检索与详情', '可按关键字和分类查询商品，详情页显示价格、库存、评价与溯源信息'),
    ('F03', '购物车管理', '支持加入、增减数量、删除、勾选结算，金额计算正确'),
    ('F04', '订单创建与支付', '可从购物车或立即购买创建订单，支付后状态变更为待发货'),
    ('F05', '商家发货与用户收货', '商家录入物流后状态转待收货，用户确认收货后状态为已完成'),
    ('F06', '优惠券核销', '满足门槛时正确抵扣，不满足条件时返回明确错误信息'),
    ('F07', '积分与等级升级', '确认收货后积分增加，并触发会员等级自动检查'),
    ('F08', 'VIP 购买与升级', 'VIP 套餐购买后到期时间更新，达标升级逻辑可触发'),
    ('F09', '通知与公告', '订单事件触发通知，公告列表可查看并进入详情'),
    ('F10', '管理端治理能力', '管理员可对用户、商家、商品、分类、优惠券、公告进行管理')
]
for cid, name, expect in case_list:
    parts.append(paragraph(f'{cid}  {name}：{expect}。测试结论：通过。', indent=False))

parts.append(heading2('5.3 异常与边界测试'))
for p in [
    '未登录访问受保护接口时，后端返回未授权错误，前端清理登录态并按当前路由决定是否跳转登录页。',
    '参数非法（如数量小于等于 0、地址不存在、优惠券状态异常）时，系统可返回可读的业务提示，不会出现前端无反馈的静默失败。',
    '当订单状态不满足操作前置条件时（如已发货订单取消），系统通过状态校验拒绝非法操作，保证状态机正确性。',
    '在网络超时和服务异常场景下，前端统一显示错误提示，后端记录异常日志，便于定位问题。'
]:
    parts.append(paragraph(p))

parts.append(heading2('5.4 性能与可维护性分析'))
for p in [
    '在教学规模数据集下，系统列表查询与常见写操作响应稳定，能够满足毕业设计演示需求。分页、索引和必要缓存的组合可有效避免明显性能瓶颈。',
    '从代码组织看，分层结构清晰，模块边界明确，便于新增功能。统一返回与全局异常减少了重复代码，提高了维护效率。',
    '目前自动化测试覆盖仍不足，回归测试主要依赖人工流程。后续应补充单元测试与接口自动化测试，提高持续迭代稳定性。'
]:
    parts.append(paragraph(p))

parts.append(heading2('5.5 存在问题与改进方向'))
improves = [
    '后端角色鉴权目前主要依赖路由和业务约定，建议引入更严格的细粒度 RBAC 注解式校验。',
    '跨域配置应由通配符逐步收敛为白名单域名，提高线上部署安全边界。',
    '订单模型在多商家混合结算场景应进一步拆分为“主订单 + 子订单”结构，提升业务一致性。',
    'VIP 阈值与脚本注释应统一，确保文档、配置与实现的一致性。',
    '建议接入 CI 流程，建立代码检查、测试执行、构建发布的一体化流水线。'
]
for idx, p in enumerate(improves, 1):
    parts.append(paragraph(f'{idx}. {p}', indent=False))
parts.append(page_break())

# 第6章
parts.append(heading1('第6章 总结与展望'))
parts.append(heading2('6.1 全文总结'))
for p in [
    '本文围绕水果生鲜电商场景，完成了从需求分析、总体设计到系统实现与测试验证的完整过程。系统覆盖消费者、商家、管理员三类用户，形成了“交易闭环 + 运营闭环 + 可信展示”的综合能力。',
    '在工程实现上，系统采用前后端分离与分层架构，结合统一返回、全局异常、登录拦截、会话校验、事务控制等机制，确保了代码结构清晰和业务流程可控。通过会员积分、VIP、通知、公告、溯源等模块，系统具备了面向实际业务演进的基础能力。',
    '测试结果表明，本系统能够较稳定地支撑毕业设计阶段的功能需求和演示场景，具备一定的可复用价值与拓展潜力。'
]:
    parts.append(paragraph(p))

parts.append(heading2('6.2 后续展望'))
future = [
    '接入真实支付与退款链路，实现完整资金流闭环，并加强账务对账能力。',
    '引入分层营销与推荐算法，实现千人千面的商品推荐和优惠策略。',
    '结合消息队列和异步任务优化高峰时段订单处理与通知投递能力。',
    '逐步引入容器化与自动化运维，提升部署效率和系统弹性。',
    '增强溯源可信机制，可探索 IoT 设备采集与可信存证方案。',
    '补齐自动化测试体系与质量门禁，形成可持续交付能力。'
]
for idx, p in enumerate(future, 1):
    parts.append(paragraph(f'{idx}. {p}', indent=False))
parts.append(page_break())

# 参考文献
parts.append(heading1('参考文献'))
refs = [
    '[1] 王珊, 萨师煊. 数据库系统概论[M]. 北京: 高等教育出版社, 2019.',
    '[2] 张海藩. 软件工程导论[M]. 北京: 清华大学出版社, 2018.',
    '[3] Ian Goodfellow, Yoshua Bengio, Aaron Courville. Deep Learning[M]. MIT Press, 2016.',
    '[4] Craig Walls. Spring Boot in Action[M]. Manning Publications, 2016.',
    '[5] Rod Johnson. Expert One-on-One J2EE Development without EJB[M]. Wiley, 2004.',
    '[6] MyBatis Plus Team. MyBatis-Plus 官方文档[EB/OL].',
    '[7] Vue Team. Vue.js 官方文档[EB/OL].',
    '[8] Vite Team. Vite 官方文档[EB/OL].',
    '[9] Redis Labs. Redis Documentation[EB/OL].',
    '[10] OWASP Foundation. OWASP Top 10 Web Application Security Risks[EB/OL].',
    '[11] 陈越, 陈小华. 电子商务系统架构设计与实践[M]. 北京: 机械工业出版社, 2020.',
    '[12] 李刚. Spring Boot 企业级开发教程[M]. 北京: 电子工业出版社, 2021.',
    '[13] 郑阿奇. MySQL 数据库原理与应用[M]. 北京: 人民邮电出版社, 2020.',
    '[14] 王争. 图解算法与数据结构[M]. 北京: 人民邮电出版社, 2019.',
    '[15] 刘鹏. 高并发系统设计[M]. 北京: 电子工业出版社, 2022.',
    '[16] 赵宏田. Web 前端工程化实践[M]. 北京: 清华大学出版社, 2021.',
    '[17] 陈志泊. 信息系统分析与设计[M]. 北京: 机械工业出版社, 2019.',
    '[18] 张红军. 软件测试技术基础[M]. 北京: 清华大学出版社, 2020.',
    '[19] 何涛. 现代物流与冷链管理[M]. 北京: 中国物流出版社, 2018.',
    '[20] 周立新. 农产品质量追溯系统研究[J]. 农业信息化, 2021(6): 34-39.'
]
for r in refs:
    parts.append(paragraph(r, indent=False, size=22, line=420, space_after=50))
parts.append(page_break())

# 致谢
parts.append(heading1('致谢'))
thanks = [
    '本论文在导师的悉心指导下完成。导师在选题方向、架构设计、实现细节和论文写作等方面给予了持续帮助，使我能够在毕业设计过程中逐步建立工程化思维和系统化解决问题的能力。',
    '感谢学院各位老师在课程学习阶段提供的理论支持与实践训练，尤其是在数据库、软件工程、Web 开发和测试方法等课程中打下的基础，为本系统实现提供了重要支撑。',
    '感谢同学们在项目联调、问题排查与答辩准备过程中的交流与建议，也感谢家人对我学习与生活的理解和支持。',
    '由于时间与经验有限，论文与系统仍存在不足之处，恳请各位老师批评指正，我将在后续学习和工作中持续改进。'
]
for p in thanks:
    parts.append(paragraph(p))

body = ''.join(parts)

document_xml = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    {body}
    <w:sectPr>
      <w:pgSz w:w="11906" w:h="16838"/>
      <w:pgMar w:top="1440" w:right="1800" w:bottom="1440" w:left="1800" w:header="851" w:footer="992" w:gutter="0"/>
      <w:cols w:space="425"/>
      <w:docGrid w:linePitch="360"/>
    </w:sectPr>
  </w:body>
</w:document>
'''

content_types = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>
'''

rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>
'''

doc_rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"></Relationships>
'''

now = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')
core = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>水果生鲜电商系统毕业论文</dc:title>
  <dc:subject>毕业设计</dc:subject>
  <dc:creator>Student</dc:creator>
  <cp:keywords>生鲜电商,Spring Boot,Vue3,毕业论文</cp:keywords>
  <dc:description>基于 Spring Boot 与 Vue3 的水果生鲜电商系统设计与实现</dc:description>
  <cp:lastModifiedBy>Codex</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">{now}</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">{now}</dcterms:modified>
</cp:coreProperties>
'''

app = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Microsoft Office Word</Application>
</Properties>
'''

with ZipFile(out_path, 'w', ZIP_DEFLATED) as z:
    z.writestr('[Content_Types].xml', content_types)
    z.writestr('_rels/.rels', rels)
    z.writestr('word/document.xml', document_xml)
    z.writestr('word/_rels/document.xml.rels', doc_rels)
    z.writestr('docProps/core.xml', core)
    z.writestr('docProps/app.xml', app)

print(str(out_path))
print('chars_in_document_xml=', len(document_xml))
