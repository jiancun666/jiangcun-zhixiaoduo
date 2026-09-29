package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.bean.MenuInfo;
import com.semple.zhixiaoduo.bean.RoleInfo;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.vo.CurrentPermissionVO;
import com.semple.zhixiaoduo.model.vo.PermissionMenuVO;
import com.semple.zhixiaoduo.model.vo.RoleOptionVO;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.utils.Result;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 当前登录账号权限接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 登录后查询当前账号生效角色、菜单树和按钮权限编码。
     * 平台账号进入企业后返回该企业真实的超级管理员角色。
     *
     * @return 处理结果。
     */
    @GetMapping("/current")
    public Result<CurrentPermissionVO> current() {
        CurrentPermissionVO response = new CurrentPermissionVO();
        LoginContext context = UserKit.getLoginContext();
        response.setPlatformAccount(context != null && context.isPlatformAccount());
        response.setClientType(UserKit.requireClientType().name());
        response.setPermissions(permissionService.listCurrentPermissionCodes());
        response.setMenus(buildTree(permissionService.listCurrentMenus()));
        List<RoleInfo> roles = permissionService.listCurrentRoles();
        response.setRoles(roles.stream()
                .map(role -> new RoleOptionVO(role.getId(), role.getRoleName(), role.getRoleType())).toList());
        return Result.success(response);
    }

    /**
     * 将平铺菜单转换为前端可直接使用的树结构。
     *
     * @param menus menus 参数。
     * @return 处理结果。
     */
    private List<PermissionMenuVO> buildTree(List<MenuInfo> menus) {
        Map<Long, PermissionMenuVO> nodes = new LinkedHashMap<>();
        menus.stream().sorted(Comparator.comparing(MenuInfo::getSortNo,
                        Comparator.nullsLast(Integer::compareTo)).thenComparing(MenuInfo::getId))
                .forEach(menu -> nodes.put(menu.getId(), toMenuVO(menu)));
        List<PermissionMenuVO> roots = new ArrayList<>();
        for (PermissionMenuVO node : nodes.values()) {
            PermissionMenuVO parent = nodes.get(node.getParentId());
            if (parent == null || node.getParentId() == null || node.getParentId() == 0) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private PermissionMenuVO toMenuVO(MenuInfo menu) {
        PermissionMenuVO response = new PermissionMenuVO();
        response.setId(menu.getId());
        response.setParentId(menu.getParentId());
        response.setMenuCode(menu.getMenuCode());
        response.setMenuName(menu.getMenuName());
        response.setMenuType(menu.getMenuType());
        response.setClientType(ClientTypeEnum.defaultPc(menu.getClientType()).name());
        response.setRoutePath(menu.getRoutePath());
        response.setComponentPath(menu.getComponentPath());
        response.setIcon(menu.getIcon());
        response.setSortNo(menu.getSortNo());
        response.setVisible(menu.getVisible());
        return response;
    }
}
