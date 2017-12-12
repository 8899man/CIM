package team.ruike.cim.service.impl;

import org.springframework.stereotype.Service;
import team.ruike.cim.dao.*;
import team.ruike.cim.pojo.*;
import team.ruike.cim.service.AdminService;
import team.ruike.cim.util.ArchivesLog;
import team.ruike.cim.util.Pager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员业务实现类
 * @author 张振国
 * @version 1.0
 */
@Service("adminService")
public class AdminServiceImpl implements AdminService {
    @Resource
    private RoleDao roleDao;
    @Resource
    private JurisdictionDao jurisdictionDao;
    @Resource
    private FunctionDao functionDao;
    @Resource
    private RoleFunctionDao roleFunctionDao;
    @Resource
    private RoleJurisdictionDao roleJurisdictionDao;
    @Resource
    private UserDao userDao;
    @Resource
    private UserRoleDao userRoleDao;
    /**
     * 获取角色列表
     * @param role 角色对象（参数）
     * @param pager 分页辅助类
     */
    @Override
    public void getRole(Role role, Pager<Role> pager) {
        pager.setTotalRecord(roleDao.selectCount(role));
        List<Role> roles = roleDao.select(role, (pager.getCurrentPage() - 1) * pager.getPageSize(), pager.getPageSize());
        pager.setList(roles);
    }

    /**
     * 获取所有权限
     * @return 权限集合
     */
    @Override
    public List<Jurisdiction> getJurisdictions() {
        return jurisdictionDao.select(new Jurisdiction(),0,99);
    }
    /**
     * 获取所有功能集合
     * @return 功能集合
     */
    @Override
    public List<Function> getFunctions() {
        return functionDao.select(new Function(),0,99);
    }

    /**
     * 新增角色
     * @param role 角色对象
     * @return 是否成功
     */
    @Override
    @ArchivesLog(operationType="新增操作",operationName="新增用户角色")
    public boolean addRole(Role role) {
        return roleDao.add(role)==1;
    }

    /**
     * 更新角色权限
     * @param jurisdictionIds 权限id数组
     * @param functionIds 功能id数组
     * @param roleId 角色id
     * @return 是否成功
     */
    @Override
    public boolean updateRoleJurisdiction(Integer[] jurisdictionIds, Integer[] functionIds, Integer roleId) {
        /*首先将此角色关联的权限与功能全部删除（因为关系表非重要数据所以这里采用真正的删除）*/
        roleFunctionDao.delete(roleId);
        roleJurisdictionDao.delete(roleId);
        /*第二部通过修改的权限id与功能id集合新增入关系表，就完成了修改*/
        for (Integer jurisdictionId : jurisdictionIds) {
            roleJurisdictionDao.add(new RoleJurisdiction(roleId,jurisdictionId));
        }
        for (Integer functionId : functionIds) {
            roleFunctionDao.add(new RoleFunction(roleId,functionId));
        }
        return true;
    }

    /**
     * 获取管理员列表
     * @param user 管理员对象（参数）
     * @param pager 分页辅助类
     */
    @Override
    public void getUsers(User user, Pager<User> pager) {
        pager.setTotalRecord(userDao.selectCount(user));
        pager.setList(userDao.select(user,(pager.getCurrentPage()-1)*pager.getPageSize(),pager.getPageSize()));
    }

    @Override
    public boolean addUser(User user, Integer roleId) {
        /*首先新增管理员*/
        userDao.add(user);
        /*再新增关联表信息*/
        List<User> users = userDao.select(user, 0, 99);
        userRoleDao.add(new UserRole(roleId,users.get(0).getUserId()));
        return true;
    }

}
