package com.github.missthee.service.imp.login;

import com.github.missthee.config.security.security.filter.UserInfoForSecurity;
import com.github.missthee.config.security.shiro.UserInfoForShiro;
import ma.glasnost.orika.MapperFacade;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.github.missthee.db.primary.mapper.basic.*;
import com.github.missthee.db.primary.model.basic.*;
import com.github.missthee.db.primary.dto.login.LoginDTO;
import com.github.missthee.service.interf.login.LoginService;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LoginImp implements LoginService, UserInfoForShiro, UserInfoForSecurity {
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final MapperFacade mapperFacade;

    @Autowired
    public LoginImp(UserMapper userMapper, UserRoleMapper userRoleMapper, RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper, PermissionMapper permissionMapper, MapperFacade mapperFacade) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.mapperFacade = mapperFacade;

    }

    @Override
    public LoginDTO selectUserByUsername(String username) {
        //查找用户
        User user;
        {
            Example userExp = new Example(User.class);
            userExp.createCriteria()
                    .andEqualTo(User.USERNAME, username);
            user = userMapper.selectOneByExample(userExp);
        }
        return getUserLoginInfo(user);
    }

    @Override
    public LoginDTO selectUserById(Integer id) {
        //查找用户
        User user;
        {
            user = userMapper.selectByPrimaryKey(id);
            if (user == null) {
                throw new UnauthenticatedException("无此账号信息");
            }
        }
        return getUserLoginInfo(user);
    }

    private LoginDTO getUserLoginInfo(User user) {
        //查找角色id集
        List<Integer> roleIdList = new ArrayList<>();
        {
            Example userRoleExp = new Example(UserRole.class);
            userRoleExp.createCriteria()
                    .andEqualTo(UserRole.USER_ID, user.getId());
            List<UserRole> userRoleList = userRoleMapper.selectByExample(userRoleExp);
            userRoleList.forEach(e -> roleIdList.add(e.getRoleId()));
        }
        //查找角色信息集
        List<Role> roleList;
        Set<String> roleValueList = new HashSet<>();
        if (roleIdList.size() > 0) {
            Example roleExp = new Example(Role.class);
            roleExp.createCriteria()
                    .andIn(Role.ID, roleIdList);
//                    .andEqualTo(Role_.IS_ENABLE, true);
            roleList = roleMapper.selectByExample(roleExp);
            roleList.forEach(e -> roleValueList.add(e.getRole()));
        }
        //查找权限id集
        List<Integer> permissionIdList = new ArrayList<>();
        if (roleIdList.size() > 0) {
            Example rolePermissionExp = new Example(RolePermission.class);
            rolePermissionExp.createCriteria()
                    .andIn(RolePermission.ROLE_ID, roleIdList);
            List<RolePermission> rolePermissionList = rolePermissionMapper.selectByExample(rolePermissionExp);
            rolePermissionList.forEach(e -> permissionIdList.add(e.getPermissionId()));
        }
        //查找权限信息集
        List<Permission> permissionList;
        Set<String> permissionValueList = new HashSet<>();
        if (permissionIdList.size() > 0) {
            Example permissionExp = new Example(Permission.class);
            permissionExp.createCriteria()
                    .andIn(Permission.ID, permissionIdList);
//                    .andEqualTo(Permission_.IS_ENABLE, true) ;
            permissionList = permissionMapper.selectByExample(permissionExp);
            permissionList.forEach(e -> permissionValueList.add(e.getPermission()));
        }

        LoginDTO loginDTO = mapperFacade.map(user, LoginDTO.class);
        loginDTO.setRoleValueList(roleValueList);
        loginDTO.setPermissionValueList(permissionValueList);
        return loginDTO;
    }

    @Override
    public SimpleAuthorizationInfo getSimpleAuthorizationInfo(Object obj) {
        LoginDTO loginDTO = selectUserById(Integer.parseInt(String.valueOf(obj)));
        return new SimpleAuthorizationInfo() {{
            addRoles(loginDTO.getRoleValueList());
            addStringPermissions(loginDTO.getPermissionValueList());
        }};
    }


    @Override
    public UserDetails loadUserById(String id) throws UsernameNotFoundException {
        LoginDTO loginDTO = selectUserById(Integer.parseInt(id));
        return transToUserDetails(loginDTO);
    }

    private UserDetails transToUserDetails(LoginDTO loginDTO) {
        if (loginDTO == null) {
            throw new UsernameNotFoundException("User not found", new Throwable());
        }
        List<String> authList = new ArrayList<String>() {{
            addAll(loginDTO.getRoleValueList().stream().map(e -> "ROLE_" + e).collect(Collectors.toSet()));
            addAll(loginDTO.getPermissionValueList());
        }};
        //权限如果前缀是ROLE_，security就会认为这是个角色信息，而不是权限，例如ROLE_MENBER就是MENBER角色，CAN_SEND就是CAN_SEND权限
        Set<SimpleGrantedAuthority> simpleGrantedAuthoritySet = authList.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        return new org.springframework.security.core.userdetails.User(String.valueOf(loginDTO.getId()), "", simpleGrantedAuthoritySet);//返回包括权限角色的User(此User为security提供的实体类)给security;
    }
}
