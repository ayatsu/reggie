package com.itheima.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Employee;
import com.itheima.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 后台员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //md5加密密码
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //根据用户名查询数据库
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        qw.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(qw);

        //如果没有查到用户则登录失败
        if(emp == null){
            return R.error("用户名不存在");
        }

        //密码对比，密码错误则登录失败
        if(!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }

        //如果是禁用status则登录失败
        if(emp.getStatus() == 0){//0禁用，1可用
            return R.error("账号已禁用");
        }

        //登录成功，将员工id存入Session并返回结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }



    /**
     * 登出功能
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");

        //返回结果
        return R.success("退出成功");
    }


    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工,员工信息"+employee.toString());

        //设置默认密码为123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        //自动填入时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        //创建人,要通过request获得
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        //存入数据库

        employeeService.save(employee);
        return R.success("新增员工成功");
    }


    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page: "+page+" pageSize: "+pageSize+" name: "+name);
        //构造分页构造器
        Page pageInfo = new Page<>(page, pageSize);

        //构造条件构造器（name
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        qw.like(StringUtils.isNotBlank(name), Employee::getName, name);

        //添加排序
        qw.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, qw);//会直接在pageInfo里封装查出来的数据
        return R.success(pageInfo);
    }


    /**
     * 通用的修改方法（包括禁用和编辑
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        log.info(employee.toString());

        //由于long有精度丢失，所以需要把employee的id转成string
        //记录更新人和更新时间
        //更新人用session获取
//        employee.setUpdateTime(LocalDateTime.now());
//        Long updateId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(updateId);

        long id = Thread.currentThread().getId();
        log.info("线程id为"+id);
        //更新
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }


    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查到对应员工信息");
    }

}
