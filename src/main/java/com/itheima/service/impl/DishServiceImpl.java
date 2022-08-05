package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.entity.SetmealDish;
import com.itheima.mapper.DishMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增菜品同时保存口味数据（插两个表
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //存dish表
        this.save(dishDto);
        //存flavor表
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();//因为flavor里没存
        for(DishFlavor flavor:flavors){
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询dishDto
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        //查dish
        Dish dish = this.getById(id);
        BeanUtils.copyProperties(dish, dishDto);
        //查dishFlavor
        LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
        qw.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(qw);
        dishDto.setFlavors(flavors);
        //查category
        LambdaQueryWrapper<Category> qw2 = new LambdaQueryWrapper<>();
        qw2.eq(Category::getId, dish.getCategoryId());
        Category category = categoryService.getOne(qw2);
        dishDto.setCategoryName(category.getName());

        return dishDto;
    }

    /**
     * 更新dish表和flavor表
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //存dish表
        this.updateById(dishDto);
        //存flavor表(先清理已有的，再按save那样新增
        Long dishId = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
        qw.eq(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(qw);

        List<DishFlavor> flavors = dishDto.getFlavors();//因为flavor里没存
        for(DishFlavor flavor:flavors){
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }
}
