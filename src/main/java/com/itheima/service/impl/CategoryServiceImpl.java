package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.mapper.CategoryMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * 根据id进行删除，但在删除之前要判断是否关联单品或套餐
     * @param id
     */
    @Override
    public void remove(Long id) {
        //是否关联单品
        LambdaQueryWrapper<Dish> dishqw = new LambdaQueryWrapper<>();
        dishqw.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishqw);
        if(count1 > 0){
            throw new CustomException("当前分类下关联单品");
        }
        //是否关联套餐
        LambdaQueryWrapper<Setmeal> setmealqw = new LambdaQueryWrapper<>();
        setmealqw.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealqw);
        if(count2 > 0){
            throw new CustomException("当前分类下关联套餐");
        }

        //正常删除
        super.removeById(id);
    }
}
