package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mappper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Override
    @Transactional
    public void sumbit(Orders orders) {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper <>();
        wrapper.eq(ShoppingCart::getUserId,currentId);
        List <ShoppingCart> list = shoppingCartService.list(wrapper);
        if (list==null||list.size()==0){
            throw new CustomException("业务异常");
        }
        User user = userService.getById(currentId);
        Long addressBookId = orders.getAddressBookId();
        AddressBook address = addressBookService.getById(addressBookId);
        if (address==null){
            throw new CustomException("地址业务异常");
        }
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails = list.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(currentId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        long orderId = IdWorker.getId();
        orders.setNumber(String.valueOf(orderId));
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(currentId);
        orders.setUserName(user.getName());
        orders.setConsignee(address.getConsignee());
        orders.setPhone(address.getPhone());
        orders.setAddress((address.getProvinceName() == null ? "" : address.getProvinceName())
                + (address.getCityName() == null ? "" : address.getCityName())
                + (address.getDistrictName() == null ? "" : address.getDistrictName())
                + (address.getDetail() == null ? "" : address.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);


        orderDetailService.saveBatch(orderDetails);


    }
}
