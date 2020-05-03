# 车牌识别Demo

使用face++的API实现的车牌识别项目Demo

## 构建运行

修改MainActivity中的`key`和`secret`即可。

## 实现

- 选择图片 - 使用Matisse实现，从结果中获取`uri`和`path`
- 拍照 - 从拍照结果中获取`uri`或`path`
- 识别 - 将图片信息放入请求体中(主要使用文件路径path)，返回后调用Gson解析结果，更新UI

1. 使用[Matisse](https://github.com/zhihu/Matisse)实现图片选择器
2. 使用[Gson](https://github.com/google/gson/)解析返回值
3. 参考[这篇文章](https://www.jianshu.com/p/ee886fc43c35)实现启动相机
4. 使用[Okhttp](https://github.com/square/okhttp/)发送POST请求



## 界面构建

LinearLayout由上而下线性布局，自定义ImageView实现在图片上绘制(重写onDraw然后需要更新时invalidate)，自定义LinearLayout以动态添加结果。

- `Toolbar(appcompat)`: 标题栏+菜单栏，菜单定义在`menu`文件夹中
- `FrameLayout`: 中间选择的图片+识别结果栏，指定布局权重(layout_weight=1)
  - `scrollview`: 图片+识别结果
  - `TextView`: 文字提示
- `FrameLayout`: 底部识别按钮
  - `LinearLayout+Button`: 识别按钮
  - `LinearLayout+ProgressBar+TextView`: 识别中进度条

## 采坑记录

1. manifest中的`android:requestLegacyExternalStorage="true"` - 使用传统的路径方式获取文件时，若不指定该项，会出现`FileNotFoundException: EACESS Permission Denied`错误
2. 注意`gradle.properties`中的代理设置 - 如果在Android Studio中设置了代理，它会询问是否让gradle也使用代理，如果gradle也使用AS的代理，那最好注意一下该文件
3. 压缩过大图片，[参考链接](https://www.jianshu.com/p/2820a10b8b85) - Face++API限制文件大小为2MB
4. Matisse需要使用最新的beta版(0.5.3-beta3)，否则可能会出问题

## 已知问题

- 动态申请权限不完善