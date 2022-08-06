package com.itheima.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Employee;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品管理
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;



    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");

    }


    /**
     * 菜品信息分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page: "+page+" pageSize: "+pageSize+" name: "+name);
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //构造条件构造器（name
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();

        //添加过滤条件
        qw.like(name != null, Dish::getName, name);
        qw.orderByAsc(Dish::getUpdateTime);

        //执行查询
        dishService.page(pageInfo, qw);
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");//records就是dish list，需要处理一下
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = new ArrayList<>();
        for(Dish record:records){
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(record, dishDto);

            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            list.add(dishDto);
        }
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 批量停售
     * @param ids
     * @return
     */

    @PostMapping("/status/0")
    public R<String> closeStatus(@RequestParam List<Long> ids){
        log.info(ids.toString());
        List<Dish> dishes = new ArrayList<>();
        for(Long id:ids){
            Dish dish = dishService.getById(id);
            dish.setStatus(0);
            dishes.add(dish);
        }
        dishService.updateBatchById(dishes);
        return R.success("选择的菜品已经批量停售");

    }

    /**
     * 批量起售
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> openStatus(@RequestParam List<Long> ids){
        log.info(ids.toString());
        List<Dish> dishes = new ArrayList<>();
        for(Long id:ids){
            Dish dish = dishService.getById(id);
            dish.setStatus(1);
            dishes.add(dish);
        }
        dishService.updateBatchById(dishes);
        return R.success("选择的菜品已经批量起售");

    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info(ids.toString());
        dishService.removeByIds(ids);
        return R.success("所选择的菜品删除成功");
    }

    /**
     *
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
//        qw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        qw.eq(Dish::getStatus, 1);//只查询状态为1的（起售
//        qw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> dishes = dishService.list(qw);
//
//        return R.success(dishes);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        qw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        qw.eq(Dish::getStatus, 1);//只查询状态为1的（起售
        qw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishes = dishService.list(qw);

        List<DishDto> dishDtos = new ArrayList<>();
        for(Dish d:dishes){
            dishDtos.add(dishService.getByIdWithFlavor(d.getId()));
        }

        return R.success(dishDtos);
    }

}
