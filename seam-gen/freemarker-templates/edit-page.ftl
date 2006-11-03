<#assign pound = "#">
<!DOCTYPE composition PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
                             "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:s="http://jboss.com/products/seam/taglib"
                xmlns:ui="http://java.sun.com/jsf/facelets"
                xmlns:f="http://java.sun.com/jsf/core"
                xmlns:h="http://java.sun.com/jsf/html"
                template="layout/template.xhtml">
                       
<ui:define name="body">

    <h1>${pageName}</h1>
    <p>Generated edit page.</p>
    
    <h:messages globalOnly="true" styleClass="message"/>
    
    <h:form id="${componentName}">
        <div class="dialog">
        <s:validateAll>
            <div class="prop">
                <span class="name">Name</span>
                <span class="value">
                    <s:decorate>
                        <h:inputText id="name" required="true"
                            value="${pound}{${componentName}Home.instance.name}"/>
                    </s:decorate>
                </span>
            </div>
        </s:validateAll>
        </div>
        <div class="actionButtons">
            <h:commandButton id="save" value="Save" 
                action="${pound}{${componentName}Home.persist}"
                rendered="${pound}{!${componentName}Home.managed}"/>     			  
            <h:commandButton id="update" value="Save" 
                action="${pound}{${componentName}Home.update}"
                rendered="${pound}{${componentName}Home.managed}"/>    			  
            <h:commandButton id="delete" value="Delete" 
                action="${pound}{${componentName}Home.remove}"
                rendered="${pound}{${componentName}Home.managed}"/>
            <s:link id="done" value="Done" linkStyle="button"
                propagation="end" view="/${masterPageName}.xhtml"/>			  
        </div>
    </h:form>
    
</ui:define>

</ui:composition>

