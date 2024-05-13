<#import "../macro/components.ftl" as components>
<#if isScenes!>
    <@components.registerDataTable categories=categories videoRegister=videoRegister showCategories=false/>
<#else>
    <@components.registerDataTable categories=categories videoRegister=videoRegister showCategories=true/>
</#if>