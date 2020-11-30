package com.coderman.api.system.controller;

import com.coderman.api.common.bean.ResponseBean;;
import com.coderman.api.common.pojo.system.User;
import com.coderman.api.system.service.UserService;
import com.coderman.api.system.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * @Author
 * @Date 2020/3/7 16:24
 * @Version 1.0
 **/

@RestController
@RequestMapping("/user")
@Validated
@Api(value = "系统用户模块", tags = "系统用户接口")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 用户登入
     *
     * @return
     */
    @ApiOperation(value = "用户登入", notes = "接收参数用户名和密码,登入成功后,返回JWTToken")
    @PostMapping("/login")
    public ResponseBean login(@RequestBody @Validated UserAddVO userAddVO,
                              HttpServletRequest request) {
        Map map=userService.login(userAddVO);
        return ResponseBean.success(map);
    }



    /**
     * 用户列表
     *
     * @return
     */
    @ApiOperation(value = "用户列表", notes = "模糊查询用户列表")
    @GetMapping("/findUserList")
    public ResponseBean findUserList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                     @RequestParam(value = "pageSize", defaultValue = "7") Integer pageSize,
                                     UserVO userVO) {
        PageVO<UserVO> userList = userService.findUserList(pageNum, pageSize, userVO);
        return ResponseBean.success(userList);
    }

    /**
     * 用户信息
     *
     * @return
     */
    @ApiOperation(value = "用户信息", notes = "用户登入信息")
    @PostMapping("/info")
    public ResponseBean info(@RequestBody User user) {
        UserVO userVO=userService.info(user);
        return ResponseBean.success(userVO);
    }


    /**
     * 更新状态
     *
     * @param id
     * @param status
     * @return
     */
    @ApiOperation(value = "用户状态", notes = "禁用和启用这两种状态")
    @PutMapping("/updateStatus/{id}/{status}")
    public ResponseBean updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        userService.updateStatus(id, status);
        return ResponseBean.success();
    }

    /**
     * 更新用户
     *
     * @param userEditVO
     * @return
     */
    @ApiOperation(value = "更新用户", notes = "更新用户信息")
    @PostMapping("/update")
    public ResponseBean update(@RequestBody @Validated UserEditVO userEditVO) {
        userService.update(userEditVO);
        return ResponseBean.success();
    }

    /**
     * 添加用户信息
     * @param userAddVO
     * @return
     */
    @ApiOperation(value = "添加用户", notes = "添加用户信息")
    @PostMapping("/add")
    public ResponseBean add(@RequestBody @Validated UserAddVO userAddVO) {
        userService.add(userAddVO);
        return ResponseBean.success();
    }


}
