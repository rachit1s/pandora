Wizards is a GWT Project that may contain one or more wizards. When making a new project, this project serves as a template.
Wizards project depends upon tbitstrunk and GWTUtils.

To make a new Wizards project follow these steps : 
1. checkout http://symphron/svn/MyNewRepository/trunk/tBitsRules/src/ksk/Wizards/
2. checkout http://symphron/svn/MyNewRepository/trunk/GWTUtils
3. import the GWTUtils project in eclipse
4. import the Wizards project in eclipse
5. add tbitstrunk to eclipse if it is not there
6. resolve all the lib dependency issues
7. Wizards would be containing some wizards already. Each one has a different package inside com.tbitsglobal.wizards.client and com.tbitsglobal.wizards.server . It is recommended that you read the classes before making a new wizard. You can delete these wizards afterwards
8. In the run configurations set -D paramaters according to your dist/build of tbits

Below is a description of classes and interfaces. Also refer to the javadocs for better explaination.


The EntryPoint class (Wizards.java in this case) instantiates the wizards according to the requirements. 
We add a new package to client and server side for a new Wizard.

Client Side : 
Every Wizard will extend the AbstractWizard.java . 
The wizard has been provided with the following buttons by default : 
1. Next
2. Back
3. Finish
Any custom buttons may be added by any page of wizard through the reference provided to them in the UIContext.

Every page of a wizard implements the interface IWizardPage<? extends LayoutContainer>
the buttons provided in the wizard are controlled by individual pages.

Client side has to have a RemoteService for database and other server side operations.

Server Side : 
The project has to have a PluginResourceServlet which implements IProxyServlet. It defines its own url_mapper to be added to the proxy servlet.
The project has to have a PluginUISegment which implements IExtUIRenderer. It return the HTML to be added to the page in the process() function.
The project has to have a DBServiceImpl which extends RemoteServiceServlet. It works as the database layer for GWT UI. It contains a static block through which it registers itself with the GWTProxyServletManager using a unique url.

Integration with tbits : 
modify the build.properties according to your machine.
and modify the build.xml accordingly.
