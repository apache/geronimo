<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright 2003-2004 The Apache Software Foundation

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

    <xsl:output method="xml" indent="yes"/>

    <!--Replace dtd with schema "attributes"-->
    <xsl:template match="ejb-jar">
        <ejb-jar xmlns="http://java.sun.com/xml/ns/j2ee"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd"
          version="2.1">
          <xsl:apply-templates/>
        </ejb-jar>

    </xsl:template>

    <xsl:template match="message-driven">
        <message-driven>
            <xsl:apply-templates select="description"/>
            <xsl:apply-templates select="display-name"/>
            <xsl:apply-templates select="small-icon"/>
            <xsl:apply-templates select="large-icon"/>
            <xsl:apply-templates select="ejb-name"/>
            <xsl:apply-templates select="ejb-class"/>
            <messaging-type>javax.jms.MessageListener</messaging-type>
            <xsl:apply-templates select="transaction-type"/>
            <activation-config>
                <xsl:if test="acknowledge-mode">
                    <activation-config-property>
                        <activation-config-property-name>acknowledgeMode</activation-config-property-name>
                        <activation-config-property-value><xsl:value-of select="acknowledge-mode"/></activation-config-property-value>
                    </activation-config-property>
                </xsl:if>
                <xsl:if test="message-selector">
                    <activation-config-property>
                        <activation-config-property-name>messageSelector</activation-config-property-name>
                        <activation-config-property-value><xsl:value-of select="message-selector"/></activation-config-property-value>
                    </activation-config-property>
                </xsl:if>
                <xsl:if test="message-driven-destination/destination-type">
                    <activation-config-property>
                        <activation-config-property-name>destinationType</activation-config-property-name>
                        <activation-config-property-value><xsl:value-of select="message-driven-destination/destination-type"/></activation-config-property-value>
                    </activation-config-property>
                </xsl:if>
                <xsl:if test="message-driven-destination/subscription-durability">
                    <activation-config-property>
                        <activation-config-property-name>subscriptionDurability</activation-config-property-name>
                        <activation-config-property-value><xsl:value-of select="message-driven-destination/subscription-durability"/></activation-config-property-value>
                    </activation-config-property>
                </xsl:if>
            </activation-config>
            <xsl:apply-templates select="env-entry"/>
            <xsl:apply-templates select="ejb-ref"/>
            <xsl:apply-templates select="ejb-local-ref"/>
            <xsl:apply-templates select="resource-ref"/>
            <xsl:apply-templates select="resource-env-ref"/>
            <xsl:apply-templates select="security-identity"/>
        </message-driven>
    </xsl:template>

    <!-- (default) copy all other elements, attributes, and text, if not matched by other rules -->
    <xsl:template match="*|@*|text()">
        <xsl:copy>
            <xsl:apply-templates select="*|@*|text()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
