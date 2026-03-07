package com.fruit.utils;

import com.fruit.entity.User;

/**
 * 用户上下文工具类（ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(User user) {
        USER_HOLDER.set(user);
    }

    public static User getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }

    public static Integer getUserType() {
        User user = getUser();
        return user != null ? user.getUserType() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
