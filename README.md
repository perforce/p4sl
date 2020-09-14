# Perforce P4SL 


## Installation

You can easily install Perforce plugin by downloading the P4SL matlab toolbox file from the [Perforce website](https://www.perforce.com/integrations).

NOTE: If you installed an earlier version of P4SL using a jar file, you must edit the classpath.txt file to delete 
the Perforce Integration lines and restart MATLAB before you install P4SL.

1. Start Matlab. Click *Open* then *Open....*  
![Open](/view/depot/main/p4sl/docs/images/open-toolbox.png)

2. Locate the P4SL.mltbx file (most likely found in your _Downloads_ folder). And click _Open_. You will see a message saying "Installation Complete". Click _Finish_.

3. The Plugin will be displayed as Installed in the Add-On Manager.   
![Plugin listed](/view/depot/main/p4sl/docs/images/plugin-listed.png)

4. To View the Plugin's information, click on the three vertical dots on the right and click _View Details_.  
![Plugin info](/view/depot/main/p4sl/docs/images/plugin-info.png)

5. Restart Matlab.

6. For Simulink projects: check that Perforce is loaded by going to:
      `New > Projects > From Source Control`
      and looking for Helix Core as one of the options in the drop-down.

Alternately, you can install the plugin the old way by editing the `classpath.txt` file.
You will need to download the *p4sl jar* file from [Perforce website](https://www.perforce.com).

1. Start Matlab.  
NOTE: Windows users may need to run MatLab as an administrator to make changes
to the `classpath.txt` file.  
![Run as admin](/view/depot/main/p4sl/docs/images/admin.png)

2. In the command window enter (without quotes): "edit classpath.txt".  
![Command](/view/depot/main/p4sl/docs/images/command.png)

3. Scroll to the bottom, and add the absolute path to your P4Simulink file.  
![Edit](/view/depot/main/p4sl/docs/images/edit.png)

4. Save the file, and *restart* Matlab.

5. For Simulink projects: check that Perforce is loaded by going to:
   `New > Projects > From Source Control`
   and looking for Helix Core as one of the options in the drop-down.

## Connecting to Perforce

The Perforce integration uses a `.p4config` file in a project's root directory
("sandbox" in Simulink) to store the user name, port, and (client) workspace.

For non Simulink projects you *MUST* create a `.p4config` file the root directory
of your project/work area.

For an existing Simulink project: if a `.p4config` file is present, it is automatically 
loaded.

If you are creating a Simulink project from scratch, the .p4config may not be present. 
You can follow these steps and a Connection Dialog will prompt for the connection fields and a
.p4config file will be written.

Sample `.p4config` file:

```
P4USER=username
P4PORT=perforceserver:1666
P4CLIENT=workspacename
```

The three values are enough for the Perforce integration to attempt to 
connect to the Perforce server. Once connected, a password will be requested
if required. If the specified workspace does not exist, P4Simulink will
attempt to create it by prompting you for a depot path. The client's root
will be set to the sandbox root (given in the Simulink project creation
window). A default view will be created, where DEPOT_PATH is the path
entered at the prompt, and CLIENT_NAME is the specified workspace name:

    //DEPOT_PATH/... //CLIENT_NAME/...


## Simulink Quick Start
### New Project from scratch
To create a new Simulink Project select *New*, *From Simulink Template*, then *Create Project* on *Simple Project*.

![New Project 1](/view/depot/main/p4sl/docs/images/new-proj1.png)

You will see a dialog like one below.

![Create Project](/view/depot/main/p4sl/docs/images/new-proj2.png)

Click on the folder icon to create a new folder (name is not editable).

![New Project Directory](/view/depot/main/p4sl/docs/images/new-proj3.png)

In the *Project* tab click on *Use Source Control*, then *Add Project to Source Control...*

![Add to Source Control](/view/depot/main/p4sl/docs/images/new-proj4.png)

Select Perforce from the Source Control Tool drop-down, then click *Change...*

![Perforce Connection](/view/depot/main/p4sl/docs/images/new-proj5.png)

Fill in the details, then click *Connect*. Upon successful connection, click *Ok*, then click *Convert*.
Edit the View Mapping and click *OK*.

![View Mapping](/view/depot/main/p4sl/docs/images/new-proj6.png)

Click *OK* on the Get Revision dialog and you will see Perforce icons on the project files.
In the *PROJECT* tab, click *Commit* and add the project to Perforce.


### Project from Source Control

To create a new Simulink Project select _New_, _Project_, then _From Simulink Template_ Or click on the *Simulink* icon on the menu bar. You will see the screen below.

![New Project](/view/depot/main/p4sl/docs/images/new_project.png)

Click on _Project from Helix Core_ or select _Helix Core_ from the _Projects_ --> _From Source Control_ dropdown.

Set the _Sandbox_ path to the location on your local machine to populate with the versioned files.  The specified path will your Perforce Workspace __root__.

![Sandbox Path](/view/depot/main/p4sl/docs/images/sandbox_path.png)

Next, set the _Repository path_, this requires a Perforce URI.  Click on the _Change..._ button to open a Connection Dialog to help generate the URI.

Provide the Server, Username and Workspace and click _Connect_ to validate and generate the URI, then _OK_ to set the URI.

![Sandbox Path](/view/depot/main/p4sl/docs/images/new-proj5.png)

Click on the _Retrieve_ button to start the process of fetching the files from Perforce.  If the sandbox directory does not exist, you will be prompted to create it, select _Yes_.

![Sandbox Path](/view/depot/main/p4sl/docs/images/retrieve.png)

If you do not already have a valid ticket session with Perforce, you will be prompted for your password...

![Sandbox Path](/view/depot/main/p4sl/docs/images/password.png)

If the Workspace specified in your connection does not exist you will be prompted to create one.  Fill out the Perforce Workspace setting, Stream or View as needed and click _OK_ to save.

![Sandbox Path](/view/depot/main/p4sl/docs/images/new-proj6.png)

Next, the plugin will request what changed to populate the workspace.  Select the change as needed, typically _Get latest revision_ is required.  Select _OK_ and Perforce will sync the specified files to your local MatLab sandbox.

![Sandbox Path](/view/depot/main/p4sl/docs/images/get_change.png)

Simulink will prompt you to create a Project, select _Yes_.

![Sandbox Path](/view/depot/main/p4sl/docs/images/create-project.png)

You will see a _Let's get started!_ popup. Click _Set Up Project_ button.

![Sandbox Path](/view/depot/main/p4sl/docs/images/setup-project1.png)

To add the project files to Simulink, click on _Add Folder..._ or _Add with Subfolders..._. When you click on _Add Folder..._, you will be prompted to select the folders you want to add to the project.

You can specify project files to automate startup tasks in step 2 as below.

![Sandbox Path](/view/depot/main/p4sl/docs/images/setup-project2.png)

You can still add files and folders to your project by selecting the folder/file to add. Select all the files and directories to add to Simulink, then right click and select _Add to Project_ or _Add Folder to Project (Including Child Files)_ as needed.

The file Status will now indicate a green tick...  

![Sandbox Path](/view/depot/main/p4sl/docs/images/add_files_4.png)

Simulink will have created/updated project files on account of the change, under _Modified Files_. To submit these back to Perforce, open the _Current Folder_ view, right click, select _Source Control_ and click _View and Commit Changes..._. 

![Sandbox Path](/view/depot/main/p4sl/docs/images/submit_proj.png)

... and provide a change description.


## MatLab Only Quick Start

You *MUST* have already created a `.p4config` file as discussed in 
Installation section and created your Perforce client workspace using
one of our clients e.g. command line `p4` or GUI `P4V` client.

MatLab automatically picks up the Perforce configuration as you
navigate files on your system.

![Files](/view/depot/main/p4sl/docs/images/files.png)

The 'Helix Core' column will indicate the file's state using a green circle 
(up-to-date) and blue square (edit) and so on...

Use the (right click) context menu -> 'Source Control' on the file or folder 
to apply Perforce actions.

![Files](/view/depot/main/p4sl/docs/images/context.png)

## Notes 

1. Clicking "Add to Project" will add the specified file(s) to the
Simulink project, and open the file for add locally. The file(s) will show
up under "Modified Files" and you must commit (submit) the file in order
to complete the process.

2. Clicking "Source Control --> Get File Lock" will open the specified file(s) for edit
within Perforce. The file(s) will show up under "Modified Files" and you
must commit (submit) the file in order to complete the process.

3. Editing a file without clicking "Get File Lock" will do nothing
unless the file is saved. Once the file has been saved and the Overwrite
button is clicked (because Perforce keeps local files as read-only by
default) the file will be opened for edit.

4. Clicking "Get File Lock" will first retrieve the latest version of the
file before opening it for edit.

5. "Remove from Project" will commit the remove action, but will *not*
delete the local file.

6. Compare against another revision will use the "p4 print" command to
stream the non-head revision to the user's machine. Local files are not
updated.

## Distribution

### Directory Structure:

```
P4Simulink
|- gradle                   Gradle build scripts
|- libs                     Simulink SCM API jar files                     
|- p4-bin                   P4D binaries used during tests
|- release                  Built JAR file
|- src
|  |- test                  Source code for functional tests
|  |- main                  Source code for the P4Simulink integration
```

## Building P4Simulink:

### Prerequisites:

You must have the following installed on the system on which you want to
build the integration:

  * JDK 1.7+
  * Gradle 2.0+

Note that gradle will download all required external dependencies as part
of the build process.

### Tests

Running the functional tests can be done via the gradle wrapper:

`./gradlew clean test`

The included tests start up a p4d instance, restore some data from checkpoint
and then run the P4Simulink functional tests against this instance. It runs
on localhost:1999.

### Building

Building the jar is done via the 'build' gradle target. Note that the process
builds a "fat" jar in that all dependent jars are included in the end result.

`./gradlew -Pver=VERSION clean build`

(`VERSION` uses the pattern REL.CHANGE e.g. `2015.1.123456`)

The target jar will appear in `build/libs/` and should follow the pattern:

`p4sl-VERSION.jar` where VERSION is the version number and set to "0" if unspecified.