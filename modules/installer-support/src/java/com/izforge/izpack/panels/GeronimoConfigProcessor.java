/**
 *
 * Copyright 2003-2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.izforge.izpack.panels;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.Pack;
import java.util.Vector;
public class GeronimoConfigProcessor {
   static int CONFIG_PROBLEM = 0;
   static int BASE_CONFIG = 1;
   static int JETTY_CONFIG = 2;
   static int TOMCAT_CONFIG = 3;
   static int EJB_CONFIG = 4;
   static int CORBA_CONFIG = 5;
   static int DERBY_CONFIG = 6;
   static int ACTIVEMQ_CONFIG = 7;
   static int LDAP_CONFIG = 8;
   static int SMTP_CONFIG = 9;
   static int CONFIG_CKPT = 10;

   static String panelNames[] = {
      // keep in sync with izpack-user-input.xml
      // until there's a better way
      "Configuration Problem",
      "Base Configuration",
      "Jetty Web Configuration",
      "Tomcat Web Configuration",
      "EJB Configuration",
      "CORBA Configuration",
      "Derby Configuration",
      "ActiveMQ Configuration",
      "Directory (LDAP) Configuration",
      "SMTP Transport Configuration",
      "Configuration Checkpoint"
   };

   static int SERVER_PACK = 0;
   static int J2EE_PACK = 1;
   static int CORBA_PACK = 2;
   static int JMS_PACK = 3;
   static int JETTY_PACK = 4;
   static int JETTY_WELCOME_PACK = 5;
   static int JETTY_MGT_PACK = 6;
   static int JETTY_UDDI_PACK = 7;
   static int JETTY_SAMPLE_PACK = 8;
   static int JETTY_DAYTRADER = 9;
   static int TOMCAT_PACK = 10;
   static int TOMCAT_WELCOME_PACK = 11;
   static int TOMCAT_MGT_PACK = 12;
   static int TOMCAT_UDDI_PACK = 13;
   static int TOMCAT_SAMPLE_PACK = 14;
   static int TOMCAT_DAYTRADER = 15;
   static int LDAP_PACK = 16;
   static int SMTP_PACK = 17;
   static int SAMPLE_DB_POOL_PACK = 18;
   static int JMX_WEB_DEBUG_PACK = 19;

   static String packNames[] = {
      "Server",
      "J2EE Features",
      "CORBA Features",
      "JMS Features",
      "Jetty Web Container",
      "Jetty Welcome Application",
      "Jetty Web Management Console",
      "Jetty UDDI Server",
      "Jetty Sample Applications",
      "Daytrader for Jetty",
      "Tomcat Web Container",
      "Tomcat Welcome Application",
      "Tomcat Web Management Console",
      "Tomcat UDDI Server",
      "Tomcat Sample Applications",
      "Daytrader for Tomcat",
      "LDAP Server",
      "SMTP Transport",
      "Sample Database Pool",
      "JMX Debug Web Application"
   };

   protected String getPanelName( int panelId ) {
      return panelNames[ panelId ];
   }

   protected String getPackName( int packId ) {
      return packNames[ packId ];
   }
   
   protected void debug( String debugString ) {
      Debug.trace( debugString );
   }
   protected static VarInfo vars[] = {

        new VarInfo( "SecurityDefaultUser",'s',BASE_CONFIG,SERVER_PACK,"", "system" ),
        new VarInfo( "SecurityDefaultPassword",'s',BASE_CONFIG, SERVER_PACK,"", "manager" ),
        new VarInfo( "PlanRemoteLoginPort",'p',BASE_CONFIG,J2EE_PACK,"Remote Login Port", "4242" ),
        new VarInfo( "PlanServerHostName",'h',JETTY_CONFIG,JETTY_PACK,"", "0.0.0.0" ),
        new VarInfo( "PlanHTTPPort",'p',JETTY_CONFIG,JETTY_PACK,"Jetty HTTP Port", "8080" ),
        new VarInfo( "PlanHTTPSPort",'p',JETTY_CONFIG, JETTY_PACK,"Jetty HTTPS Port", "8443"),
        new VarInfo( "PlanAJPPort",'p',JETTY_CONFIG,JETTY_PACK,"Jetty AJP Port", "8009" ),
        new VarInfo( "PlanServerHostName",'h',TOMCAT_CONFIG,TOMCAT_PACK,"", "0.0.0.0" ),
        new VarInfo( "PlanHTTPPort2",'p',TOMCAT_CONFIG,TOMCAT_PACK,"Tomcat HTTP Port", "8080" ),
        new VarInfo( "PlanHTTPSPort2",'p',TOMCAT_CONFIG,TOMCAT_PACK,"Tomcat HTTPS Port", "8443" ),
        new VarInfo( "PlanAJPPort2",'p',TOMCAT_CONFIG,TOMCAT_PACK,"Tomcat AJP Port", "8009" ),
        new VarInfo( "PlanNamingPort",'p',EJB_CONFIG,SERVER_PACK,"Naming Port", "1009" ),
        new VarInfo( "PlanOpenEJBPort",'p',EJB_CONFIG, J2EE_PACK,"EJB Port", "4201" ),
        new VarInfo( "PlanClientAddresses",'s',EJB_CONFIG,SERVER_PACK,"", "0.0.0.0" ),
        new VarInfo( "PlanIIOPPort",'p',CORBA_CONFIG,CORBA_PACK,"IIOP Port", "9000" ),
        new VarInfo( "PlanORBSSLHost",'h',CORBA_CONFIG,CORBA_PACK,"", "localhost" ),
        new VarInfo( "PlanORBSSLPort",'p',CORBA_CONFIG,CORBA_PACK,"ORB SSL Port", "2001" ),
        new VarInfo( "PlanCOSNamingHost",'h',CORBA_CONFIG,CORBA_PACK,"", "localhost" ),
        new VarInfo( "PlanCOSNamingPort",'p',CORBA_CONFIG,CORBA_PACK,"CosNaming Port", "1050" ),
        new VarInfo( "PlanDerbyPort",'p',DERBY_CONFIG,SAMPLE_DB_POOL_PACK,"Derby Port", "1527" ),
        new VarInfo( "PlanActiveMQPort",'p',ACTIVEMQ_CONFIG,JMS_PACK,"JMS Port", "61616" ),
        new VarInfo( "PlanLdapPort",'p',LDAP_CONFIG,LDAP_PACK,"LDAP Port", "1389" ),
        new VarInfo( "PlanSMTPHost",'h',SMTP_CONFIG,SMTP_PACK,"SMTP Host", "localhost" ),
        new VarInfo( "PlanSMTPPort",'p',SMTP_CONFIG,SMTP_PACK,"SMTP Port", "25" )
    };

    protected boolean isAdvancedMode( AutomatedInstallData idata ) {
       boolean fRet = false;
       String val = idata.getVariable( "advanced.mode" );
       if( val.equalsIgnoreCase( "true" )) fRet = true;
       return fRet;
    }
    
    protected boolean isTomcatSelected( AutomatedInstallData idata ) {
       return isPackSelected( idata, packNames[ TOMCAT_PACK ]);
    }
    protected boolean isPackSelected( AutomatedInstallData idata, String packName ) {
       boolean fRet = false;
       int iSize = idata.selectedPacks.size();
       if( iSize > 0 ) {
          for( int i = 0; i < iSize; ++i ) {
             Pack p = (Pack)idata.selectedPacks.get( i );
             if( p.name.equals( packName )) {
                fRet = true;
             }
          }
       }
       return fRet;
    }

    protected boolean isJettySelected( AutomatedInstallData idata ) {
       return isPackSelected( idata, packNames[ JETTY_PACK ]);
    }


    protected void panelNavDebug( AutomatedInstallData idata ) {
       debug( "--> num panels: " + idata.panels.size() + "  cur panel: " + idata.curPanelNumber );
    }
    protected boolean isPackSelectionProblem( AutomatedInstallData idata ) {
       boolean fRet = false;
       if( isAdvancedMode( idata ) == true ) {
          fRet = false; // this assumes all pack selections are OK in advanced mode
       } else if( isJettySelected( idata ) && isTomcatSelected( idata )) {
            fRet = true;
       }
       return fRet;
    }
    protected boolean shouldSkipPanel( AutomatedInstallData idata, String panelName ) {
      boolean fRet = false;
      if( panelName.equalsIgnoreCase( getPanelName( CONFIG_PROBLEM ))) {
         if( isPackSelectionProblem( idata )) 
            fRet = false;
         else fRet = true;
      }
      debug( "shouldSkipPanel( " + panelName + " ) is: " + fRet );
      return fRet;
    }

    protected void initEnableVarsToPackSelections( AutomatedInstallData idata ) {
        boolean fSel = false;
        for( int i = 0; i < idata.allPacks.size(); ++i ) {
            Pack pack = (Pack)idata.allPacks.get( i );
            fSel = false;
            if( isPackSelected( idata, pack.name )) {
                fSel = true;
            }
            setEnableVar( idata, pack.name, fSel );
        }
    }

    protected static boolean fInitialConfigDone = false;
    protected void setInitialConfig( AutomatedInstallData idata ) {
         if( fInitialConfigDone ) return;
         fInitialConfigDone = true;
         debug( ">setInitialConfig()  ");
         //System.out.println( "CPES = " + cpes );

         initEnableVarsToPackSelections( idata );

         if( isAdvancedMode( idata )) {
         // fixup for jetty not selected to install
            if( isTomcatSelected( idata ) == true ) {
                int packs[] = {
                    TOMCAT_PACK, 
                    TOMCAT_WELCOME_PACK,
                    TOMCAT_MGT_PACK,
                    TOMCAT_UDDI_PACK,
                    TOMCAT_SAMPLE_PACK,
                    TOMCAT_DAYTRADER };
                 debug( "-setInitialConfig() : tomcat is selected for install. ");
                 boolean fSel = false;
                 String packName = null;
                 if( isJettySelected( idata ) == false ) {
                    debug( "-setInitialConfig() : jetty is not selected for install. ");
                    for( int i = 0; i < packs.length; ++i ) {
                        packName = getPackName( packs[ i ] );
                        fSel = isPackSelected( idata, packName );
                        setEnableVar( idata, packName, fSel );
                    }
                 } else {
                    debug( "-setInitialConfig() : jetty is selected for install. ");
                    for( int i = 0; i < packs.length; ++i ) {
                        packName = getPackName( packs[ i ] );
                        setEnableVar( idata, packName, false );
                    }
                 }
            } else {
                debug( "-setInitialConfig() : tomcat is not selected for install. ");
            }
         }
         // end fixup

         // setup initial variable values in case the operator never hits the 
         // panel where the default value gets set. This way we don't wind
         // up with thinks like ${PlanServerName} in the config.xml -- even
         // though it doesn't affect operations since this only happens
         // for disabled configs (load=false)
         // Don't set any "xxx.enable" variables this way...
         for( int i = 0; i < vars.length; ++i ) {
            vars[ i ].setDefault( idata );
         }
         debug( "<setInitialConfig()  ");
    }

    protected void setEnableVar( AutomatedInstallData idata, String packName, boolean fSetting ) {
       debug( ">setEnableVar()");
       String varName = new String(packName);
       varName = varName.replace( ' ','.' );
       varName += ".enable";
       String sel = "false";
       if( fSetting ) {
          sel = "true";
       }
       Debug.trace( varName + " is " + sel );
       idata.setVariable( varName, sel );
       debug( "<setEnableVar()");
    }

    protected void resetConfigNonSelected( AutomatedInstallData idata ) {
         debug( ">resetConfigNonSelected()  ");
         String packName = null;
         for( int i = 0; i < idata.allPacks.size(); ++i ) {
                Pack pack = (Pack)idata.allPacks.get( i );
                packName = pack.name;
                if( isPackSelected( idata, packName ) == false ) {
                   String sel = "false";
                   String varName = new String(packName);
                   varName = varName.replace( ' ','.' );
                   varName += ".enable";
                   Debug.trace( varName + " is " + sel );
                   idata.setVariable( varName, sel );
                }
         }
         debug( "<resetConfigNonSelected()  ");
    }
    protected boolean panelEntryTasks( AutomatedInstallData idata, String panelName ) {
       boolean fRet = true;
       if( panelName.equals( getPanelName( BASE_CONFIG ))) {
          setInitialConfig( idata ); // from selected packs
          if( isAdvancedMode( idata ) == false ) {
             initEnableVarsToPackSelections( idata );
          } else { 
             resetConfigNonSelected( idata );
          }
       }
       return fRet;
    }
    protected boolean checkInput( String panelName, AutomatedInstallData idata ) {
      boolean fRet = true; // default to true in case a panel is added and
                           // we forget to update this method ;)
      if( panelName.equals( getPanelName( BASE_CONFIG ))) {
          fRet = true;
       }
       else if( panelName.equals( getPanelName( JETTY_CONFIG ))) {
          fRet = true;
       }
       else if( panelName.equals( getPanelName( TOMCAT_CONFIG ))) {
          fRet = true;
       }
       else if( panelName.equals( getPanelName( EJB_CONFIG ))) {
          fRet = true;
       }
       else if( panelName.equals( getPanelName( CORBA_CONFIG ))) {
          fRet = true;
       }
       else if( panelName.equals( getPanelName( DERBY_CONFIG ))) {
          fRet = true;
       }
       else if( panelName.equals( getPanelName( ACTIVEMQ_CONFIG ))) {
          fRet = true;
       }
       else if( panelName.equals( getPanelName( LDAP_CONFIG ))) {
          fRet = true;
       }
       else if( panelName.equals( getPanelName( SMTP_CONFIG ))) {
          fRet = true;
       }
       else if( panelName.equals( getPanelName( CONFIG_CKPT ))) {
          fRet = true;
          setAutoInstallConfig( idata );
          processConfig( idata );
       }
       return fRet;
    }

    protected boolean processConfig( AutomatedInstallData idata ) {

       boolean fRet = true;

       /* kind of a hack to work around the fact that 
          if any web container is installed, then all the samples
          both for jetty and tomcat are desired.  
          !!!! Left this code in, but it's not currently set up to really work.
       */
       String samples = "false";
       String varName = "Geronimo.Sample.Applications";
       if( isPackSelected( idata, packNames[ JETTY_SAMPLE_PACK ] )) {
          samples = "true";
       }
       if( isPackSelected( idata, packNames[ TOMCAT_SAMPLE_PACK ] )) {
          samples = "true";
       }
       idata.setVariable( varName, samples ); 

       String serverName = null;
       String webBuilderNamespace = null;
       varName = null;
       if( isTomcatSelected( idata ) ) {
          serverName = idata.getVariable( "TOMCAT_WEB_SERVER_NAME" );
          varName = "PlanWebServerName";
          idata.setVariable( varName, serverName );
          webBuilderNamespace = idata.getVariable( "TOMCAT_WEBBUILDER_NAMESPACE" );
          varName = "PlanWebBuilderDefaultNamespace";
          idata.setVariable( varName, webBuilderNamespace );
       } else if( isJettySelected( idata ) ) {
          serverName = idata.getVariable( "JETTY_WEB_SERVER_NAME" );
          varName = "PlanWebServerName";
          idata.setVariable( varName, serverName );
          webBuilderNamespace = idata.getVariable( "JETTY_WEBBUILDER_NAMESPACE" );
          varName = "PlanWebBuilderDefaultNamespace";
          idata.setVariable( varName, webBuilderNamespace );
       } else {
          // no web container was selected.  Set some reasonable default so
          // the user can at least figure out how to modify the value
          serverName = idata.getVariable( "NO_WEB_SERVER_NAME" );
          varName = "PlanWebServerName";
          idata.setVariable( varName, serverName );
          webBuilderNamespace = idata.getVariable( "NO_WEBBUILDER_NAMESPACE" );
          varName = "PlanWebBuilderDefaultNamespace";
          idata.setVariable( varName, webBuilderNamespace );
       }
       return fRet;
    }

    protected void setAutoInstallConfig( AutomatedInstallData idata ) {
         debug( ">setAutoInstallConfig()  ");

         // variables used in processing (izpack-process.xml)
         String cpes = System.getProperty( "path.separator" );
         idata.setVariable( "CPES", cpes );
         String trace = System.getProperty( "TRACE" );
         if( trace == null ) {
            trace = "false";
         }
         idata.setVariable( "TRACE", trace );

         // the server pack is always selected and its 
         // enable variable MUST be true.  This is the default
         // for the interactive install, but we must fix it up
         // for the automated install. Most of the ".enable" 
         // variables are defined as checkbox variables
         // in associated panels.  The server pack does
         // not have one of these, but there is a config.xml
         // dependency on this enable variable.
         String serverPack = getPackName( SERVER_PACK );
         serverPack = serverPack.replace( ' ', '.' );
         String serverEnableVar = serverPack + ".enable";
         idata.setVariable( serverEnableVar, "true" );

         // setup variables for each pack and set each according
         // to whether the pack is selected or not.
         // e.g. pack="J2EE Features", var="J2EE.Features"
         //                     var is true if J2EE Features pack
         //                     will be installed and false otherwise.
         //    For interactive installs, these variables already exist.
         // Additionally, define a variable packname.enable for
         //    each pack which denotes whether it's enabled at runtime
         //    in config.xml.
         //    At this point in the proceedure, some of these may already
         //    be defined.  So, we only create the ones for non-selected
         //    packs which may not have had panels displayed. In any case,
         //    if the pack is not selected for install, the related
         //    PackName.enable variable should be false.
         String packName = null;
         for( int i = 0; i < idata.allPacks.size(); ++i ) {
                Pack pack = (Pack)idata.allPacks.get( i );
                String sel = "false";
                packName = pack.name;
                String packVar = new String(packName);
                packVar = packVar.replace( ' ','.' );
                sel = idata.getVariable( packVar );
                if( sel == null ) {
                   sel = "false";
                   idata.setVariable( packVar, sel );
                }
                if( isPackSelected( idata, packName )) {
                   sel = "true";
                   idata.setVariable( packVar, sel );
                }
                String packEnable = packVar + ".enable";
                String eSel = idata.getVariable( packEnable );
                // only set undefined enable variables
                if( eSel == null ) {
                   eSel = "false";
                   idata.setVariable( packEnable, eSel );
                }
                debug( "-- " + packVar + " is: " + sel + "   " + packEnable + " is: " + eSel );
         }

         debug( "<setAutoInstallConfig()  ");
    }

    protected void processAutomatedInput( 
                 AutomatedInstallData idata, String panelName ) {
 
         if( isCheckpointPanel( panelName )) {
            setAutoInstallConfig( idata );
            processConfig( idata );
         }
    }
    protected boolean isCheckpointPanel( String panelName ) {
       boolean fRet = panelName.equals( getPanelName( CONFIG_CKPT ));
       debug( "isCheckpointPanel( " + panelName + " ) is: " + fRet );
       return fRet;
    }
    protected boolean haveConfigErrors(
           AutomatedInstallData idata, String msgs[] ) {
       int numVars2Ck = 0;
       for( int i = 0; i < vars.length; ++i ) {
          if( vars[i].verTyp == 'p' && isPackSelected( idata, packNames[ vars[i].packId] ))
             ++numVars2Ck;
       }
       int vars2Ck[] = new int[ numVars2Ck ];
       int idx = 0;
       for( int i = 0; i < vars.length; ++i ) {
          if( vars[i].verTyp == 'p' && isPackSelected( idata, packNames[ vars[i].packId] )) {
             vars2Ck[idx] = i;
             ++idx;
          }
       }
       Vector conflicts = new Vector();
       int vars2Ck2[] = new int[ numVars2Ck ];
       System.arraycopy( vars2Ck, 0, vars2Ck2, 0, numVars2Ck );
       for( int i = 0; i < vars2Ck.length; ++i ) {
          for( int j = 0; j < vars2Ck2.length; ++j ) {
            int p1Idx = vars2Ck[i];
            int p2Idx = vars2Ck2[j];            
            if( p1Idx == p2Idx ) continue; // filter out same
            String p1 = idata.getVariable( vars[p1Idx].varName );
            String p2 = idata.getVariable( vars[p2Idx].varName );
            if( p1 != null && p2 != null && p1.equals(p2) ) {
               conflicts.add( new Object[] { new Integer(p1Idx), new Integer(p2Idx) });
            }
          }
       }
       boolean fRet = false;
       int numConflicts = conflicts.size();
       Object conflictDups[] = new Object[numConflicts];
       if( numConflicts > 0 ) {
          fRet = true;
          int msgIdx = 0;
          msgs[msgIdx] = "Warning. Geronimo configuration problems.";
          msgs[++msgIdx] = "       The list of port conflicts below should be resolved before continuing the installation.";
          msgs[++msgIdx] = "__________________________________";
          int dupsIdx = 0;
          boolean fDup = false;
          for( int i = 0; i < numConflicts && i < 7; ++i ) {
              Object[] ports = (Object[])conflicts.elementAt(i);
              int p1Idx = ((Integer)ports[0]).intValue();
              int p2Idx = ((Integer)ports[1]).intValue();
              fDup = false;
              for( int dupCk = 0; dupCk < conflictDups.length; ++dupCk ) {
                 Object[] ports2 = (Object[])conflictDups[dupCk];
                 if( ports2 != null ) {
                    int v1 = ((Integer)ports2[0]).intValue();
                    int v2 = ((Integer)ports2[1]).intValue();
                    if(( v1 == p1Idx && v2 == p2Idx ) || ( v1 == p2Idx && v2 == p1Idx ))
                       fDup = true;
                 }
              }
              conflictDups[dupsIdx] = ports;
              ++dupsIdx;
              if( fDup == false ) {
                 msgs[++msgIdx] = "    The \"" + vars[p1Idx].varDesc + "\" conflicts with the \"" + vars[p2Idx].varDesc + "\"";
              }
          }
          ++msgIdx;
          for( int i = msgIdx; i < 10; ++i ) {
             msgs[i] = " ";
          }
       }
       return fRet;
    }
    protected void panelDebug( AutomatedInstallData idata, String panelName ) {
      Debug.trace( "------ Begin panel entry debug info for: " + panelName + "-----" );
      if( panelName.equals( getPanelName( BASE_CONFIG ))) {
         for( int i = 0; i < idata.selectedPacks.size(); ++i ) {
            Pack pack = (Pack)idata.selectedPacks.get( i );
            Debug.trace( pack.name + " is selected." );
         }
         Debug.trace( "-----------" );
         String packName = null;
         for( int i = 0; i < idata.allPacks.size(); ++i ) {
                Pack pack = (Pack)idata.allPacks.get( i );
                String sel = "not selected";
                packName = pack.name;
                if( isPackSelected( idata, packName )) {
                   sel = "selected";
                }
                Debug.trace( packName + " is " + sel );
         }
      }
      Debug.trace( "------ End panel entry debug info for: " + panelName + "-----" );
    }
}
class VarInfo {
   public VarInfo( String varName, char verTyp, int panelId, int packId, String varDesc, String defVal ) {
      this.varName = varName;
      this.panelId = panelId;
      this.packId = packId;
      this.verTyp = verTyp;
      this.varDesc = varDesc;
      this.defVal = defVal;
   }
   public void setDefault( AutomatedInstallData idata ) {
      idata.setVariable( varName, defVal );
   }
   String varName;
   int panelId;
   int packId;
   char verTyp;
   String varDesc;
   String defVal;
}
