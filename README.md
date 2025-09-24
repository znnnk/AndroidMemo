# AndroidMemo - 智能备忘录应用

一个功能丰富的Android备忘录应用，支持事项管理、提醒通知、分类标签和收藏功能。

## 📱 应用信息

- **应用名称**: 备忘录
- **包名**: com.example.androidmemo
- **版本**: 1.0
- **目标平台**: Android 15.0 (API 35)
- **最低支持**: Android 15.0 (API 35)
- **开发语言**: Java
- **数据库**: SQLite

## ✨ 核心功能

### 1. 事项管理
- ✅ **创建事项**: 添加新的待办事项，包含标题、内容、提醒时间
- ✅ **编辑事项**: 修改已有事项的详细信息
- ✅ **查看事项**: 点击查看事项详情
- ✅ **删除事项**: 长按删除不需要的事项
- ✅ **状态管理**: 支持标记事项为已完成/未完成

### 2. 分类标签系统
- 📋 **未处理列表**: 显示所有待办事项
- ✅ **已处理列表**: 显示已完成的事项
- ⭐ **收藏夹**: 重要事项置顶显示
- 🏠 **首页**: 综合视图，显示所有事项

### 3. 智能提醒功能
- ⏰ **定时提醒**: 设置具体的提醒日期和时间
- 🔔 **通知栏提醒**: 自动在通知栏显示提醒
- 📱 **AlarmManager**: 使用系统闹钟服务确保提醒准确性
- 🔄 **开机自启**: 支持开机后自动恢复提醒功能

### 4. 收藏功能
- ⭐ **重要标记**: 标记重要事项为收藏
- 📌 **置顶显示**: 收藏事项优先显示
- 🗂️ **独立管理**: 专门的收藏夹页面

## 🛠️ 技术特性

### 架构设计
- **Fragment架构**: 使用Fragment实现页面切换
- **SQLite数据库**: 本地数据持久化存储
- **BroadcastReceiver**: 处理系统广播和自定义提醒
- **Service**: 后台服务支持

### 权限管理
- `POST_NOTIFICATIONS`: 通知权限
- `SCHEDULE_EXACT_ALARM`: 精确闹钟权限
- `RECEIVE_BOOT_COMPLETED`: 开机启动权限

### 数据库设计
```sql
-- 主事项表
tb_ToDoItem:
- _id: 主键，自增
- remindTitle: 事项标题
- createDate: 创建时间
- modifyDate: 修改时间
- remindText: 事项内容
- remindDate: 提醒时间
- haveDo: 是否已完成
- isFavorite: 是否收藏

-- 提醒管理表
tb_Remind:
- _id: 主键，自增
- remindID: 关联事项ID
- notificationID: 通知ID
```

## 📂 项目结构

```
app/src/main/java/com/example/androidmemo/
├── MainActivity.java          # 主活动，管理Fragment切换
├── MyDBOpenHelper.java        # 数据库帮助类
├── AlarmReceiver.java         # 闹钟广播接收器
├── TimeService.java           # 时间服务
├── StartNotification.java     # 通知管理
├── BaseFragment.java          # Fragment基类
├── Add.java                   # 添加事项页面
├── Update.java                # 编辑事项页面
├── RemindList.java            # 首页列表
├── UndoList.java              # 未处理事项列表
├── DoneList.java              # 已处理事项列表
└── FavoriteList.java          # 收藏夹列表
```

## 🎨 界面设计

### 主界面
- 简洁的Fragment容器设计
- 顶部菜单栏提供快速操作
- 响应式布局适配不同屏幕

### 菜单功能
- **添加新事项**: 跳转到添加页面
- **删除全部事项**: 清空所有数据（带确认对话框）
- **分类处理**: 子菜单包含未处理/已处理
- **回到首页**: 返回主列表
- **收藏夹**: 快速访问收藏事项
- **退出**: 安全退出应用

## 🚀 安装与使用

### 环境要求
- Android Studio 最新版本
- Android SDK API 35
- Java 11+

### 构建步骤
1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 同步Gradle依赖
4. 连接Android设备或启动模拟器
5. 点击运行按钮构建并安装应用

### 使用说明
1. **添加事项**: 点击菜单"添加新事项"，填写标题、内容、提醒时间
2. **查看事项**: 在列表中点击任意事项查看详情
3. **编辑事项**: 在详情页面点击编辑按钮
4. **删除事项**: 长按事项选择删除
5. **标记完成**: 在事项详情中切换完成状态
6. **收藏事项**: 在事项详情中点击收藏按钮
7. **设置提醒**: 在添加/编辑页面选择提醒时间

## 🔧 主要改进

相比原版项目（https://github.com/SoonFa/AndroidMemo)，本应用进行了以下优化：

1. **UI优化**: 重新设计界面，提升用户体验
2. **提醒机制**: 将TimerTask改为AlarmManager，提高提醒可靠性
3. **收藏功能**: 新增收藏系统，重要事项置顶显示
4. **权限管理**: 完善Android 13+通知权限处理
5. **数据库升级**: 支持数据库版本升级，添加收藏字段
6. **错误处理**: 增强异常处理和日志记录

## 📋 依赖库

```gradle
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.10.0'
implementation 'androidx.activity:activity:1.8.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
```


---

*一个简单而强大的Android备忘录应用，让生活更有序！* 📱✨