package pro.beam.api.services.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ListenableFuture;
import pro.beam.api.BeamAPI;
import pro.beam.api.exceptions.BeamException;
import pro.beam.api.http.BeamHttpClient;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.response.users.UserFollowsResponse;
import pro.beam.api.response.users.UserSearchResponse;
import pro.beam.api.services.AbstractHTTPService;
import pro.beam.api.util.Users;

import java.util.Map;

public class UsersService extends AbstractHTTPService {
    public UsersService(BeamAPI beam) {
        super(beam, "users");
    }

    public ListenableFuture<BeamUser> findOne(int id) {
        return this.get(String.valueOf(id), BeamUser.class);
    }

    public ListenableFuture<BeamUser> refresh() {
        return this.post("current/refresh", BeamUser.class);
    }

    /**
     * Login without two factor authentication
     * @param username The user's username
     * @param password The user's password
     * @return
     */
    public CheckedFuture<BeamUser, BeamException> login(String username, String password) {
        ListenableFuture<BeamUser> result = this.post("login", BeamUser.class, new ImmutableMap.Builder<String, Object>()
                                                .put("username", username)
                                                .put("password", password).build());

        return Users.checkFutureUser(result);
    }

    /**
     * Login with two factor authentication.
     *
     * @param username The user's username
     * @param password The user's password
     * @param authCode The user's two factor authentication code
     * @return
     */
    public CheckedFuture<BeamUser, BeamException> login(String username, String password, String authCode) {
        if (authCode.length() != 6) {
            throw new IllegalArgumentException("two factor authentication code have to be 6 digits (was " + authCode.length() + ")");
        } else {
            ListenableFuture<BeamUser> result = this.post("login", BeamUser.class, new ImmutableMap.Builder<String, Object>()
                                                        .put("username", username)
                                                        .put("password", password)
                                                        .put("code", authCode).build());

            return Users.checkFutureUser(result);
        }
    }

    public ListenableFuture<String> logout() {
        return this.delete("current", null);
    }

    public ListenableFuture<UserSearchResponse> search(String query) {
        if (query != null && query.length() < 3) {
            throw new IllegalArgumentException("unable to preform search with query less than 3 characters (was "+query.length()+")");
        } else {
            Map<String, Object> args = BeamHttpClient.getArgumentsBuilder().put("query", query).build();

            return this.get("search", UserSearchResponse.class, args);
        }
    }

    public ListenableFuture<UserFollowsResponse> following(BeamUser user, int page, int limit) {
        return this.post(user.id + "/follows",
                         UserFollowsResponse.class,
                         BeamHttpClient.getArgumentsBuilder()
                                 .put("page", Math.max(0, page))
                                 .put("limit", Math.min(limit, 50)).build());
    }
}
