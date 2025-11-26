package org.markus.rhserver.entity.vo;

import org.markus.rhserver.model.Group;
import org.markus.rhserver.model.Users;

import java.util.List;

public record FriendList(List<Users> users, List<Group> groups) {
}
