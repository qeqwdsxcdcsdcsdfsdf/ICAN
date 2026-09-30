# Debug Session: login-success-crash

## Basic Information
- **Session ID**: login-success-crash
- **Status**: [OPEN]
- **Created**: 2026-07-06
- **Bug Description**: 用户登录成功后应用闪退，黑屏退出
- **Reproduction Steps**:
  1. 打开应用进入登录页
  2. 输入正确工号和密码
  3. 点击登录按钮
  4. 登录成功提示后闪退

## Hypotheses

| # | Hypothesis | Status | Evidence |
|---|------------|--------|----------|
| 1 | MainActivity 的 setContentView/findViewById 因资源缺失导致崩溃 | PENDING | 等待运行时证据 |
| 2 | SignInFragment 加载布局时因资源或空指针导致崩溃 | PENDING | 等待运行时证据 |
| 3 | BottomNavigationView 因主题不兼容或菜单资源问题崩溃 | PENDING | 等待运行时证据 |
| 4 | App 类的 setupExceptionHandler 在捕获异常时出现问题 | PENDING | 等待运行时证据 |

## Instrumentation Plan

1. 在 LoginActivity 登录成功后跳转前添加日志
2. 在 MainActivity onCreate 关键节点添加日志
3. 在 SignInFragment onCreateView 关键节点添加日志
4. 在 App 类异常处理器中增强日志

## Evidence Logs

### Pre-Fix Logs

*(等待用户复现)*

### Post-Fix Logs

*(修复后收集)*

## Fix Solution

*(基于证据)*

## Verification

*(验证结果)*
