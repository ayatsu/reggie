package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.mapper.SetmealMapper;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增setmeal和setmeal_dish表
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存setmeal表
        this.save(setmealDto);
        //保存setmeal_dish表,由于setmealid没有值(刚刚创建)所以需要一一赋值
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(SetmealDish dish:setmealDishes){
            dish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除setmeal和setmeal_dish
     * @param ids
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，只能删停售中的
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.in(Setmeal::getId, ids);
        qw.eq(Setmeal::getStatus, 1);//在售中
        int count = this.count(qw);
        if(count > 0){
            throw new CustomException("套餐在售中，无法删除");
        }
        //删除setmeal
        this.removeByIds(ids);

        //删除setmeal_dish里的
        LambdaQueryWrapper<SetmealDish> qw2 = new LambdaQueryWrapper<>();
        qw2.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(qw2);
    }
}
