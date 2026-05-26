package com.example.photoGroupe.dto.follow;

public class FollowStatsDTO {

    private Long    userId;
    private String  username;
    private long    followersCount;
    private long    followingCount;
    private boolean isFollowedByCurrentUser;  // current user → target
    private boolean isFollowingCurrentUser;   // target → current user
    private boolean isBlockingThem;           // current user has blocked target
    private boolean isBlockedByThem;          // target has blocked current user

    public FollowStatsDTO() {}

    public FollowStatsDTO(Long userId,
                          String username,
                          long followersCount,
                          long followingCount,
                          boolean isFollowedByCurrentUser,
                          boolean isFollowingCurrentUser,
                          boolean isBlockingThem,
                          boolean isBlockedByThem) {
        this.userId                  = userId;
        this.username                = username;
        this.followersCount          = followersCount;
        this.followingCount          = followingCount;
        this.isFollowedByCurrentUser = isFollowedByCurrentUser;
        this.isFollowingCurrentUser  = isFollowingCurrentUser;
        this.isBlockingThem          = isBlockingThem;
        this.isBlockedByThem         = isBlockedByThem;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────

    public Long    getUserId()                  { return userId; }
    public String  getUsername()                { return username; }
    public long    getFollowersCount()          { return followersCount; }
    public long    getFollowingCount()          { return followingCount; }
    public boolean isFollowedByCurrentUser()    { return isFollowedByCurrentUser; }
    public boolean isFollowingCurrentUser()     { return isFollowingCurrentUser; }
    public boolean isBlockingThem()             { return isBlockingThem; }
    public boolean isBlockedByThem()            { return isBlockedByThem; }

    public void setUserId(Long userId)                           { this.userId = userId; }
    public void setUsername(String username)                     { this.username = username; }
    public void setFollowersCount(long v)                        { this.followersCount = v; }
    public void setFollowingCount(long v)                        { this.followingCount = v; }
    public void setFollowedByCurrentUser(boolean v)              { this.isFollowedByCurrentUser = v; }
    public void setFollowingCurrentUser(boolean v)               { this.isFollowingCurrentUser = v; }
    public void setBlockingThem(boolean v)                       { this.isBlockingThem = v; }
    public void setBlockedByThem(boolean v)                      { this.isBlockedByThem = v; }
}