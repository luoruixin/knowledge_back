package com.ikun.knowledge_back.utils;

public class PermissionJudge {
    public static int judgeAuthority(Integer type, Integer examine){
        if(type==null&&examine==null){
            //表示未进行实名认证
            return 0;
        }else if(type==1&&examine==0){
            //表示刚刚实名认证，还未绑定房屋
            //这里不可能出现type=0 && examine=0的情况，因为其业主一定在绑定房屋之后才进行的添加家人的操作
            //只能点击添加房屋按钮
            return 1;
        } else if (type==0&&examine==1) {
            //表示这是普通居民，并且其业主已经绑定房屋
            //此时除了投票权限和添加家人的权限之外其他权限都有(可以点击车辆管理等按钮)
            return 2;
        }else if (type==1&&examine==1){
            //表示这是业主，并且已经绑定房屋
            //此时有投票的权限(可以点击参与投票按钮)
            return 3;
        }else if(type==2&&examine==1){
            //表示这是业委会成员
            return 4;
        }
        return -1;
    }
}
