# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for?
This is the GIT repository for [VoxelShop](https://blackflux.com/node/11). If you have questions about the program itself, please do not create an issue, but ask on the [forums](https://blackflux.com/forums/).

## Setup, Run Locally and Distributable

### Required Tools
Git, IntelliJ and JDK7 (e.g. http://downloads.puresoftware.org/files/android/JDK/)

### Git
Fork the git repository, open a terminal, go to the desired project location and type:
~~~~
> git init
> git remote add origin [FORK_HTTPS_URL]
> git pull origin master
~~~~
then enter your password and wait until command completes

### IntelliJ
- Click Open and select PS4k Folder (within the folder you selected as root)
- Ensure the JDK7 is selected under File > Project Structure > Project
- Right click on PS4k > src > com > vitco > Main and "Run" to start VoxelShop
- **Important**: Click on Main and on "Edit Configuration" and add "debug" as a program argument

### Build Distributable
- Click Build > Build Artifacts > All Artifacts
- All content needed to run PS4k is under PS4k > out > artifacts > PS4k_jar

## Dependencies

### Jide
Java UI component provider and Window Manager
http://www.jidesoft.com/

## How to Contribute

### Adding a new JIDE Frame
#### Adding the Frame to the layout
- Execute jide_designer.jar under Tools/jide_designer and open PS4k\resource\layout\TopLayout.ilayout
- Under Design, click the "+ Frame" button
- For the added Frame update the ID, the Titles (starting with "# ") and set showContextMenu and autohidable to False
- If you want to change the layout, do it through the properties window. Drag and drop changes are not saved (!).
- Finally save the file and close Jide Designer
- When starting VoxelShop now you will see a warning that no linkage is defined for the frame. We need to fix that!

#### Linking the Frame to the Program
- Under PS4k > src > com > vitco > layout > frames create a new java file called "[FRAME_ID]Linkage". This class will create some logic for the Frame we just created.
- Compare how other Linkages work. The important bits are constructing the frame and updating the title. We also need to define a "show" action for the frame.
- We will need a 16x16 png icon for the frame. Create it under PS4k > resource > img > icons > frames. This icon is shown when the frame is tabbed.
- Now open to PS4k > src > com > vitco > glue > config.xml and add the linkage similar to the existing ones.
- Now define the Frame titles by adding them as "[FRAME_ID]_caption" to PS4k > resource > bundle > LangBundle.properties

#### Allowing to restore a Closed Frame
- Open PS4k > src > com > vitco > layout > bars > main_menu.xml and link the show action to to a new menu item under menu item "tool_windows_btn".
- Start the program and ensure we can toggle the visibility of the frame through the main menu

#### Link a shortcut map for the Frame
- Open PS4k > src > com > vitco > glue > shortcuts.xml
- Add the missing frames in here. This is where we will specify frame specific shortcuts.

#### Adding Content to the Frame
- Create a new package "[FRAME_ID]" under PS4k > src > com > vitco > layout > content
- Create a Class "[FRAME_ID]View" extending ViewPrototype and an implemented Interface "[FRAME_ID]ViewInterface" that contains a method "JComponent build(Frame frame)"
- In the Linkage class define a setter method for the Interface (we will inject the class shortly) and after updating the title, call frame.add([INJECTED_VARIABLE].build(mainFrame));
- Now we need to inject the class into the Linkage Class. Open PS4k > src > com > vitco > glue > config.xml and create a new bean from the class we just defined with the build method. Then inject the property into the frame bean we defined previously.
- Now we can start working on our GUI in the build method.

#### Adding a Tool Bar
- Define an xml file, load it and attach it to the layout
- Define actions and link to them in the xml file

-----------
*-- Below is auto generated*

### How do I get set up?
* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines
* Writing tests
* Code review
* Other guidelines

### Who do I talk to?
* Repo owner or admin
* Other community or team contact