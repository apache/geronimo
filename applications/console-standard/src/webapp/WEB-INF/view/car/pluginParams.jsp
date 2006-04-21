<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>Export Plugin</b> -- Configure Plugin Data</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="configure-after" />
    <input type="hidden" name="configId" value="${configId}" />
    <table border="0">


        <!-- ENTRY FIELD: Name -->
          <tr>
            <th style="min-width: 140px"><div align="right">Human Readable Name:</div></th>
            <td><input name="name" type="text" size="30" value="${name}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A human-readable name that will be displayed for this plugin.
            </td>
          </tr>
        <!-- ENTRY FIELD: Config ID -->
          <tr>
            <th style="min-width: 140px"><div align="right">Unique ID:</div></th>
            <td><b>${configId}</b></td>
          </tr>
          <tr>
            <td></td>
            <td>
              The globally unique ID for this plugin.  This is determined from
              the installation in the server you're exporting.  This defines
              the version number for the plugin, so make sure it's correct.
            </td>
          </tr>
        <!-- ENTRY FIELD: Source Repository -->
          <tr>
            <th style="min-width: 140px"><div align="right">Download Repository:</div></th>
            <td><input name="sourceRepository" type="text" size="30" value="${sourceRepository}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              The main repository that should be checked when downloading dependencies
              for this plugin.  This should be a URL, such as
              <tt>http://geronimoplugins.com/repository/</tt>
            </td>
          </tr>
        <!-- ENTRY FIELD: Backup Repository -->
          <tr>
            <th style="min-width: 140px"><div align="right">Additional Repositories:</div></th>
            <td><textarea rows="5" cols="60" name="backupRepository">${backupRepository}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A list of additional repositories to check for any dependencies that are
              not present in the main repository.  This should be a list of one URL per
              line, with values such as <tt>http://www.ibiblio.org/maven2/</tt>
            </td>
          </tr>
        <!-- ENTRY FIELD: Category -->
          <tr>
            <th style="min-width: 140px"><div align="right">Category:</div></th>
            <td><input name="category" type="text" size="30" value="${category}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              The category this plugin falls into.  Plugins in the same category will
              be listed together.  If this plugin is intended to be listed on
              geronimoplugins.com then you should use one of the category names there
              if any of them fit.  Otherwise, you can select this freely, or according
              to the categories acceptable to the repository where you plan to post
              this.
            </td>
          </tr>
        <!-- ENTRY FIELD: Description -->
          <tr>
            <th style="min-width: 140px"><div align="right">Description:</div></th>
            <td><textarea rows="10" cols="60" name="description">${description}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A description of this plugin.  You should use plain text only, with
              blank lines to separate paragraphs.
            </td>
          </tr>
        <!-- ENTRY FIELD: License -->
          <tr>
            <th style="min-width: 140px"><div align="right">License:</div></th>
            <td><input name="license" type="text" size="30" value="${license}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              The name of the license that this plugin is covered by.  Ideally, it would
              be prefixed by the class of license, like "BSD -- (name)" or "GPL -- (name)".
            </td>
          </tr>
        <!-- ENTRY FIELD: License Is Open Source-->
          <tr>
            <th style="min-width: 140px"><div align="right">Open Source:</div></th>
            <td>
                <input type="checkbox" name="licenseOSI"<c:if test="${!(empty licenseOSI)}"> checked="checked"</c:if> />
            </td>
          </tr>
          <tr>
            <td></td>
            <td>
              Check this box if the license is an OSI-approved open source license
              (see <a href="http://www.opensource.org/licenses/index.php">http://www.opensource.org/licenses/index.php</a>).
            </td>
          </tr>
        <!-- ENTRY FIELD: Geronimo Versions -->
          <tr>
            <th style="min-width: 140px"><div align="right">Geronimo Versions:</div></th>
            <td><textarea rows="5" cols="60" name="gerVersions">${gerVersions}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
              An optional list of Geronimo versions supported by this plugin.  If no values
              are listed, the plugin can be installed in any version of Geronimo.  Otherwise,
              list one acceptable Geronimo version per line, like
              "1.1&nbsp;\n&nbsp;1.1.1&nbsp;\n&nbsp;1.1.2&nbsp;\n&nbsp;..."
              (ideally, of course, this means you've actually tested the plugin with each
              Geronimo version listed here).
            </td>
          </tr>
        <!-- ENTRY FIELD: JVM Versions -->
          <tr>
            <th style="min-width: 140px"><div align="right">JVM Versions:</div></th>
            <td><textarea rows="5" cols="60" name="jvmVersions">${jvmVersions}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
              An optional list of JVM version prefixes supported by this plugin.  If no values
              are listed, the plugin can be installed in Geronimo running in any version of
              the JVM.  Otherwise, list one acceptable JVM version perfix per line, like
              "1.4.2&nbsp;\n&nbsp;1.5&nbsp;\n&nbsp;..."
              (ideally, of course, this means you've actually tested the plugin with Geronimo
              on each JVM version listed here).
            </td>
          </tr>
        <!-- ENTRY FIELD: Dependencies -->
          <tr>
            <th style="min-width: 140px"><div align="right">Dependencies:</div></th>
            <td><textarea rows="5" cols="60" name="dependencies">${dependencies}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A list of JARs or other module IDs that this plugin depends on.  These
              will be downloaded automatically when this plugin is installed.  Normally
              you shouldn't change this list.  However, you can move entries from the
              dependency list to the <b>prerequisite</b> list if the user must install the
              dependency manually before installing the plugin (e.g. for a database pool
              where a plugin wouldn't know what server to connect to).<br /><br />
              Each entry in this list should use the Unique ID format like is used for
              this plugin above.  You may remove the version number if you'd like to
              work with any version of the dependency, though that may be risky.  Each
              value should be on a separate line.
            </td>
          </tr>
        <!-- ENTRY FIELD: Obsoletes -->
          <tr>
            <th style="min-width: 140px"><div align="right">Obsoletes:</div></th>
            <td><textarea rows="5" cols="60" name="obsoletes">${obsoletes}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A list of module IDs that this plugin replaces.  Those plugins or
              modules will be removed when this one is installed.  That may include
              previous versions of this plugin if you want installing it to "upgrade"
              rather than just offering an additional alternative.  This should be a
              list with one module ID per line.
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 1 -->
          <tr>
            <th style="min-width: 140px"><div align="right">Prerequisite 1 ID:</div></th>
            <td><input name="prereq1" type="text" size="30" value="${prereq1}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              The module ID of a prerequisite for this plugin.  This is a module that
              must be present in the server before the plugin can be installed.  It
              may be something like a specific web container for a web application
              (<tt>geronimo/jetty/*/car</tt>) or something like a database pool or
              security realm that the user must install because the plugin author can't
              create a value that will be valid in the destination server.  You may want
              to leave out as many segments of the module ID as possible in order to
              accomodate more users (e.g. <tt>*/mypool/*/*</tt> rather than
              <tt>myapp/mypool/1.2/car</tt>).
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 1 Type -->
          <tr>
            <th style="min-width: 140px"><div align="right">Prerequisite 1 Type:</div></th>
            <td><input name="prereq1type" type="text" size="30" value="${prereq1type}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A brief description of the type of prerequisite this is (for the benefit
              of the user).  Examples could include <tt>Database Pool</tt> or
              <tt>Web Container</tt>.
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 1 Description -->
          <tr>
            <th style="min-width: 140px"><div align="right">Prereq 1 Description:</div></th>
            <td><textarea rows="5" cols="60" name="prereq1desc">${prereq1desc}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A longer description of what the user needs to do to comply with this
              prerequisite (for example, instructions to set up a database pool listing
              the supported database products and telling the user where to find a script
              to initialize the database).  This should be plain text with empty lines
              to separate paragraphs.
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 2 -->
          <tr>
            <th style="min-width: 140px"><div align="right">Prerequisite 2 ID:</div></th>
            <td><input name="prereq2" type="text" size="30" value="${prereq2}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              The module ID of a prerequisite for this plugin.  This is a module that
              must be present in the server before the plugin can be installed.  It
              may be something like a specific web container for a web application
              (<tt>geronimo/jetty/*/car</tt>) or something like a database pool or
              security realm that the user must install because the plugin author can't
              create a value that will be valid in the destination server.  You may want
              to leave out as many segments of the module ID as possible in order to
              accomodate more users (e.g. <tt>*/mypool/*/*</tt> rather than
              <tt>myapp/mypool/1.2/car</tt>).
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 2 Type -->
          <tr>
            <th style="min-width: 140px"><div align="right">Prerequisite 2 Type:</div></th>
            <td><input name="prereq2type" type="text" size="30" value="${prereq2type}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A brief description of the type of prerequisite this is (for the benefit
              of the user).  Examples could include <tt>Database Pool</tt> or
              <tt>Web Container</tt>.
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 2 Description -->
          <tr>
            <th style="min-width: 140px"><div align="right">Prereq 2 Description:</div></th>
            <td><textarea rows="5" cols="60" name="prereq2desc">${prereq2desc}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A longer description of what the user needs to do to comply with this
              prerequisite (for example, instructions to set up a database pool listing
              the supported database products and telling the user where to find a script
              to initialize the database).  This should be plain text with empty lines
              to separate paragraphs.
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 3 -->
          <tr>
            <th style="min-width: 140px"><div align="right">Prerequisite 3 ID:</div></th>
            <td><input name="prereq3" type="text" size="30" value="${prereq3}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              The module ID of a prerequisite for this plugin.  This is a module that
              must be present in the server before the plugin can be installed.  It
              may be something like a specific web container for a web application
              (<tt>geronimo/jetty/*/car</tt>) or something like a database pool or
              security realm that the user must install because the plugin author can't
              create a value that will be valid in the destination server.  You may want
              to leave out as many segments of the module ID as possible in order to
              accomodate more users (e.g. <tt>*/mypool/*/*</tt> rather than
              <tt>myapp/mypool/1.2/car</tt>).
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 3 Type -->
          <tr>
            <th style="min-width: 140px"><div align="right">Prerequisite 3 Type:</div></th>
            <td><input name="prereq3type" type="text" size="30" value="${prereq3type}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A brief description of the type of prerequisite this is (for the benefit
              of the user).  Examples could include <tt>Database Pool</tt> or
              <tt>Web Container</tt>.
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 3 Description -->
          <tr>
            <th style="min-width: 140px"><div align="right">Prereq 3 Description:</div></th>
            <td><textarea rows="5" cols="60" name="prereq3desc">${prereq3desc}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
              A longer description of what the user needs to do to comply with this
              prerequisite (for example, instructions to set up a database pool listing
              the supported database products and telling the user where to find a script
              to initialize the database).  This should be plain text with empty lines
              to separate paragraphs.
            </td>
          </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value="Save Plugin Data" /></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index" />
            </portlet:actionURL>">Cancel</a></p>
