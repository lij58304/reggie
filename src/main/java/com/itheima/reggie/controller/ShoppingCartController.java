package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R <ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //设置用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper <>();
        wrapper.eq(ShoppingCart::getUserId,currentId);
        if (shoppingCart.getDishId()!=null){
            wrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(wrapper);
        if (one!=null){
            Integer oneNumber = one.getNumber();
            one.setNumber(oneNumber+1);
            shoppingCartService.updateById(one);
        }else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }
        return R.success(one);
    }

    @PostMapping("/sub")
    public R <ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        //设置用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper <>();
        wrapper.eq(ShoppingCart::getUserId,currentId);
        if (shoppingCart.getDishId()!=null){
            wrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(wrapper);
        Integer oneNumber = one.getNumber();
        one.setNumber(oneNumber-1);
        if (one.getNumber()!=0){
            shoppingCartService.updateById(one);
        }else {
            shoppingCartService.removeById(one.getId());
            return R.success(null);
        }

        return R.success(one);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List <ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper <>();
        wrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        wrapper.orderByAsc(ShoppingCart::getCreateTime);
        List <ShoppingCart> list = shoppingCartService.list(wrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
