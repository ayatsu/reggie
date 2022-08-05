package com.itheima.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Category;
import com.itheima.entity.Employee;
import com.itheima.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;


    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增菜品,菜品信息"+category.toString());

        categoryService.save(category);
        return R.success("新增菜品成功");
    }


    /**
     * 菜品/套餐分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        log.info("page: "+page+" pageSize: "+pageSize+" name: ");
        //构造分页构造器
        Page pageInfo = new Page<>(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<>();

        //添加排序
        qw.orderByAsc(Category::getSort);

        //执行查询
        categoryService.page(pageInfo, qw);//会直接在pageInfo里封装查出来的数据
        return R.success(pageInfo);
    }


    /**
     * 根据id删除菜系
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除分类，id为"+ids);

        categoryService.remove(ids);
        return R.success("菜品/套餐分类成功删除");
    }


    /**
     * 修改菜品
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update( @RequestBody Category category){
        //更新
        categoryService.updateById(category);
        return R.success("菜品信息修改成功");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<>();
        qw.eq(category.getType() != null, Category::getType, category.getType());
        qw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(qw);

        return R.success(list);
    }

}
