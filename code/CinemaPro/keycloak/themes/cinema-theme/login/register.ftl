<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Register</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/custom-login.css">
</head>
<body>

<div class="container">

    <!-- LEFT SIDE -->
    <div class="left">
        <div class="card">

            <div class="title">
                Create account
            </div>

            <form id="kc-register-form"
                  action="${url.registrationAction}"
                  method="post">

                <!-- FIRST NAME -->
                <input
                    type="text"
                    name="firstName"
                    placeholder="First name"
                    class="input"
                    value="${(register.formData.firstName!'')}"
                />

                <#if messagesPerField.existsError('firstName')>
                    <div class="error">${kcSanitize(messagesPerField.get('firstName'))?no_esc}</div>
                </#if>

                <!-- LAST NAME -->
                <input
                    type="text"
                    name="lastName"
                    placeholder="Last name"
                    class="input"
                    value="${(register.formData.lastName!'')}"
                />

                <#if messagesPerField.existsError('lastName')>
                    <div class="error">${kcSanitize(messagesPerField.get('lastName'))?no_esc}</div>
                </#if>

                <!-- USERNAME -->
                <input
                    type="text"
                    name="username"
                    placeholder="Username"
                    class="input"
                    value="${(register.formData.username!'')}"
                />

                <#if messagesPerField.existsError('username')>
                    <div class="error">${kcSanitize(messagesPerField.get('username'))?no_esc}</div>
                </#if>

                <!-- EMAIL -->
                <input
                    type="email"
                    name="email"
                    placeholder="Email"
                    class="input"
                    value="${(register.formData.email!'')}"
                />

                <#if messagesPerField.existsError('email')>
                    <div class="error">${kcSanitize(messagesPerField.get('email'))?no_esc}</div>
                </#if>

                <!-- PASSWORD -->
                <input
                    type="password"
                    name="password"
                    placeholder="Password"
                    class="input"
                />

                <#if messagesPerField.existsError('password')>
                    <div class="error">${kcSanitize(messagesPerField.get('password'))?no_esc}</div>
                </#if>

                <!-- CONFIRM PASSWORD -->
                <input
                    type="password"
                    name="password-confirm"
                    placeholder="Confirm password"
                    class="input"
                />

                <#if messagesPerField.existsError('password-confirm')>
                    <div class="error">${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}</div>
                </#if>

                <!-- GLOBAL ERROR -->
                <#if message?has_content>
                    <div class="error">
                        ${kcSanitize(message.summary)?no_esc}
                    </div>
                </#if>

                <!-- SUBMIT -->
                <button type="submit" class="button">
                    Sign up
                </button>

                <!-- LINKS -->
                <div class="links">
                    <a href="${url.loginUrl}">
                        Already have an account? Sign in
                    </a>
                </div>

            </form>

        </div>
    </div>

    <!-- RIGHT SIDE -->
    <div class="right">
        <div class="brand">
            🎬 CinemaPro
        </div>
    </div>

</div>

</body>
</html>