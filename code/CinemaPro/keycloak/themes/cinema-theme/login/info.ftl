<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Information</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/custom-login.css">
</head>
<body>

<div class="container">

    <!-- LEFT -->
    <div class="left">
        <div class="card">

            <div class="title">
                ${kcSanitize(message.summary)?no_esc}
            </div>

            <#if message.type == 'success'>
                <div class="info-text">
                    Please check your email and follow the instructions.
                </div>
            </#if>

            <#if message.type == 'error'>
                <div class="error">
                    ${kcSanitize(message.summary)?no_esc}
                </div>
            </#if>

            <div class="links">
                <a href="${url.loginUrl}">
                    Back to login
                </a>
            </div>

        </div>
    </div>

    <!-- RIGHT -->
    <div class="right">
        <div class="brand">
            🎬 Cinema
        </div>
    </div>

</div>

</body>
</html>
