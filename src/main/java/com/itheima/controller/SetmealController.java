package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;



    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 查询分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page: "+page+" pageSize: "+pageSize+" name: "+name);
        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dishDtoPage = new Page<>();

        //构造条件构造器（name
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();

        //添加过滤条件
        qw.like(name != null, Setmeal::getName, name);
        qw.orderByAsc(Setmeal::getUpdateTime);

        //执行查询
        setmealService.page(pageInfo,qw);
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = new ArrayList<>();
        for(Setmeal record:records){
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(record, setmealDto);

            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            list.add(setmealDto);
        }
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }




    /**
     * 批量停售
     * @param ids
     * @return
     */

    @PostMapping("/status/0")
    public R<String> closeStatus(@RequestParam List<Long> ids){
        log.info(ids.toString());
        List<Setmeal> setmeals = new ArrayList<>();
        for(Long id:ids){
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(0);
            setmeals.add(setmeal);
        }
        setmealService.updateBatchById(setmeals);
        return R.success("选择的套餐已经批量停售");

    }

    /**
     * 批量起售
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> openStatus(@RequestParam List<Long> ids){
        log.info(ids.toString());
        List<Setmeal> setmeals = new ArrayList<>();
        for(Long id:ids){
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(1);
            setmeals.add(setmeal);
        }
        setmealService.updateBatchById(setmeals);
        return R.success("选择的套餐已经批量起售");

    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info(ids.toString());
        setmealService.removeWithDish(ids);
        return R.success("所选择的菜品删除成功");
    }

    /**
     * 根据categoryId查当前套餐分类下的套餐们
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        qw.eq(Setmeal::getStatus, 1);//只查询状态为1的（起售
        qw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmeals = setmealService.list(qw);

        return R.success(setmeals);
    }

    /**
     * 根据setmealId查setmealDish的情况
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> listDish(@PathVariable Long id){
        log.info(id.toString());
        LambdaQueryWrapper<SetmealDish> qw = new LambdaQueryWrapper<>();
        qw.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(qw);
        List<DishDto> dishDtos = new ArrayList<>();
        for(SetmealDish setmealDish:setmealDishes){
            DishDto dishDto = dishService.getByIdWithFlavor(setmealDish.getDishId());
            dishDto.setCopies(setmealDish.getCopies());
            dishDtos.add(dishDto);
        }



        return R.success(dishDtos);
    }

}
