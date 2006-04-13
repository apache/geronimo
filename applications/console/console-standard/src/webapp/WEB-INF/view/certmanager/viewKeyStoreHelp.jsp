 <p>This portlet allows the user to list public key certificates, import trusted certificates, generate a key pair, generate a Certificate Signing Request (CSR) and import a Certificate Authority (CA) reply. This portlet also provides information about the type of keystore, the number of key pairs and trusted certificates stored in the keystore (the keystore size), and the location of the keystore.<br>
  <br>
  From the main portlet, the user can &quot;view&quot; a trusted certificate or 
  key pair. <br>
  <br>
  <span style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;">import trusted certificate</span></p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="20">&nbsp;</td>
    <td>If the user has a trusted certificate, it can be imported using the&quot;import trusted certificate&quot; tool. Browse for the certificate file, click on the &quot;View Certificate&quot; button, enter an alias, in the&quot;Alias&quot; text box, then click on the &quot;Import&quot; button. &quot;Cancel&quot; at any time before clicking on &quot;Import.&quot; </td>
  </tr>
</table>
<p><span style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;">generate key pair</span></p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="20">&nbsp;</td>
    <td><p>This tool allows the user to generate a public/private key pair. This key pair could be used later to create a trusted certificate. Fill in the text boxes and click on the &quot;submit&quot; button to generate the key pair. All fields can be blanked out by clicking &quot;reset.&quot; To generate the key pair, click on &quot;submit.&quot; The fields are defined as follows:</p>
      <table width="100%"  border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top">Alias</td>
          <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">A name to identify the key pair.</td>
        </tr>
        <tr>
          <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top">Validity</td>
          <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">The number of days the key pair will remain valid. This field requires a number to be entered.</td>
        </tr>
      </table>
      <p> The following fields are X.500 Distinguished Names.</p>
      <table width="100%"  border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top">Common Name (CN)</td>
          <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">Common name of a person, such as, Joe Smith.</td>
        </tr>
        <tr>
          <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" align="right" valign="top"> Organizational Unit(OU)</td>
          <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">Small organization, e.g., department or division, for example, Sales.</td>
        </tr>
        <tr>
          <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top"> Organization Name(ON)</td>
          <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">Large organization, such as the company name.</td>
        </tr>
        <tr>
          <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top"> Locality (L)</td>
          <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">City name, e.g., El Segundo.</td>
        </tr>
        <tr>
          <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top"> State (ST)</td>
          <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">State name, e.g., California or CA.</td>
        </tr>
        <tr>
          <td class="MediumBackground" style="padding: 10px 10px 10px 5px; color: #1E1E52;" width="150" align="right" valign="top"> Country (C)</td>
          <td class="LightBackground" style="padding: 10px 5px 10px 10px" valign="top">Two-letter country code, e.g., US.</td>
        </tr>
      </table>    </td>
  </tr>
</table>
<p><span style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;">view</span></p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="20">&nbsp;</td>
    <td><p>To view a key pair or certificate, click on the &quot;view&quot; link to the left of it. If the user is viewing a key pair, the &quot;view&quot; pane provides access to three more links &quot;generate CSR&quot;, &quot;import CA reply&quot;, and &quot;keystore list&quot; which are defined next:</p>
	  <br /><span style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;">generate CSR</span><br />
      <table width="100%"  border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td width="20">&nbsp;</td>
          <td>This tool generates a Certificate Signing Request. The CSR can be sent to a Certificate Authority (CA), such as Verisign. To submit the CSR to a CA follow the CA's instructions. After the CA sends back a reply, the Gluecode Standard Edition console can be used to import it via the &quot;import CA reply&quot; tool.</td>
        </tr>
      </table>
	  <br /><span style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;">import CA reply</span><br />
      <table width="100%"  border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td width="20">&nbsp;</td>
          <td>After the Certificate Authority (CA) has sent back a signed, trusted certificate, it can be imported into the Gluecode Standard Edition server. Cut and paste the Certificate Authority's reply in this window and click on &quot;Save.&quot; To cancel an import click on &quot;Cancel&quot; before doing a &quot;Save.&quot; </td>
        </tr>
      </table>
	  <br /><span style="padding: 10px 10px 10px 5px; font-size: 10px; color: #546BC7; text-decoration: underline; font-weight: bold;">keystore list</span><br />
      <table width="100%"  border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td width="20">&nbsp;</td>
          <td>This link returns the user to the main Certificate management portlet.</td>
        </tr>
      </table>
    </td>
  </tr>
</table>
<p><br>
</p>
