package io.spongebob.apiserver.domain;

public enum Roles {
    ROLE_PLATFORM_ADMIN(1000), ROLE_GROUP_ADMIN(100), ROLE_USER(10);

    private int level;

    private Roles(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }
}