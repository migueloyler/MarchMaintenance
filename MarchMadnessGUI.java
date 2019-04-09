//package marchmadness;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.ButtonBar.ButtonData;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Modality;

/**
 *  MarchMadnessGUI
 * 
 * this class contains the buttons the user interacts
 * with and controls the actions of other objects 
 *
 * @author Grant Osborn
 */
public class MarchMadnessGUI extends Application {
    
    
    //all the gui ellements
    private BorderPane root;
    private ToolBar toolBar;
    private ToolBar btoolBar;
    private Button simulate;
    private Button login;
    private Button scoreBoardButton;
    private Button viewBracketButton;
    private Button clearButton;
    private Button resetButton;
    private Button finalizeButton;
    private Button instructionButton;
    
    //allows you to navigate back to division selection screen
    private Button back;
  
    
    private  Bracket startingBracket; 
    //reference to currently logged in bracket
    private Bracket selectedBracket;
    private Bracket simResultBracket;

    
    private ArrayList<Bracket> playerBrackets;
    private HashMap<String, Bracket> playerMap;

    

    private ScoreBoardTable scoreBoard;
    private TableView table;
    private BracketPane bracketPane;
    private GridPane loginP;
    private TournamentInfo teamInfo;
    
    
    @Override
    public void start(Stage primaryStage) {
        //try to load all the files, if there is an error display it
        try{
            teamInfo=new TournamentInfo();
            startingBracket= new Bracket(TournamentInfo.loadStartingBracket());
            simResultBracket=new Bracket(TournamentInfo.loadStartingBracket());
        } catch (IOException ex) {
            showError(new Exception("Can't find "+ex.getMessage(),ex),true);
        }
        //deserialize stored brackets
        playerBrackets = loadBrackets();
        
        playerMap = new HashMap<>();
        addAllToMap();
        


        //the main layout container
        root = new BorderPane();
        scoreBoard= new ScoreBoardTable();
        table=scoreBoard.start();
        loginP=createLogin();
        CreateToolBars();
        
        //display login screen
        login();
        
        setActions();
        //root.setTop(toolBar);   
        root.setBottom(btoolBar);
        Scene scene = new Scene(root);
        primaryStage.setMaximized(true);
        
        instructionButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
             public void handle(ActionEvent event) {
                 final Stage instructionPage = new Stage(); //creates a new stage for the popup
                 instructionPage.initModality(Modality.WINDOW_MODAL);  //Makes pop up a modal dialogue box

                 Button exitButton = new Button("Exit"); //close button is created inside of instructionButton dialogue
                 exitButton.setOnAction(new EventHandler<ActionEvent>(){ //sets what exitButton button does

                     @Override
                     public void handle(ActionEvent f) {
                         instructionPage.close(); //closes dialogue
                     }
            });
                 String instructionText = "HELLO! WELCOME TO THE MARCH MADNESS BRACKET SIMULATOR!\n\n"
                         + "To begin please select either fill out the four divisions, or do it all at once using the full bracket\n\n"
                         +"The \"Choose Division\" button will allow you to navigate between the various divisions, or choose the full bracket\n\n"
                         + "To fill out the bracket just click on the team you wish to advance\n\n"
                         + "A team's statistics can be seen by right clicking on the team's name on the bracket \n\n"
                         + "WARNING: if you change a team's advancement on a previous round after filling everything out, all future rounds will be affected as well\n\n"
                         + "If you wish to clear the entire bracket just click the \"Reset\" button on the bottom\n\n"
                         + "Once the entire bracket has been filled out, the \"Finalize\" button can be clicked, which will lock the users choices\n\n"
                         + "The top tool bar will also become available to the the user, which will allow the user to either logout, or simulate the game via the \"Simulate\" button\n\n"
                         + "Please be aware that once a game has been simulated no more players may create a bracket for that game\n\n"
                         + "Players can see their scores by clicking on the \"ScoreBoard\" button or see the final bracket using \"View Simulated Bracket\"\n\n";
                 
                 Scene Instructions = new Scene(VBoxBuilder.create()
                         .children(new Text(instructionText), exitButton) //instructionsText string and exitButton button are placed in the scene
                         .alignment(Pos.BOTTOM_CENTER) //Placement of "exitButton" button
                         .padding(new Insets(50)) //essentially sets size of window
                         .build());
                 
                 instructionPage.setTitle("Instructions"); //names the stage
                 instructionPage.setScene(Instructions); //sets the scene within the stage
                 instructionPage.show();
             }
         });

        primaryStage.setTitle("March Madness Bracket Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    
    
    /**
     * simulates the tournament  
     * simulation happens only once and
     * after the simulation no more users can login
     */
    private void simulate(){
        //cant login and restart prog after simulate
        login.setDisable(true);
        simulate.setDisable(true);
        
       scoreBoardButton.setDisable(false);
       viewBracketButton.setDisable(false);
       
       teamInfo.simulate(simResultBracket);
       for(Bracket b:playerBrackets){
           scoreBoard.addPlayer(b,b.scoreBracket(simResultBracket));
       }
        
        displayPane(table);
    }
    
    /**
     * Displays the login screen
     * 
     */
    private void login(){            
        login.setDisable(true);
        simulate.setDisable(true);
        scoreBoardButton.setDisable(true);
        viewBracketButton.setDisable(true);
        btoolBar.setDisable(true);
        displayPane(loginP);
    }
    
     /**
     * Displays the score board
     * 
     */
    private void scoreBoard(){
        displayPane(table);
    }
    
     /**
      * Displays Simulated Bracket
      * 
      */
    private void viewBracket(){
       selectedBracket=simResultBracket;
       bracketPane=new BracketPane(selectedBracket);
       GridPane full = bracketPane.getFullPane();
       full.setAlignment(Pos.CENTER);
       full.setDisable(true);
       displayPane(new ScrollPane(full)); 
    }
    
    /**
     * allows user to choose bracket
     * 
     */
   private void chooseBracket(){
        //login.setDisable(true);
        btoolBar.setDisable(false);
        bracketPane=new BracketPane(selectedBracket);
        displayPane(bracketPane);

    }
    /**
     * resets current selected sub tree
     * for final4 reset Ro2 and winner
     */
    private void clear(){
      
      if (bracketPane.clear() == 1) {
          //bracketPane.clear();
          bracketPane=new BracketPane(selectedBracket);
          displayPane(bracketPane);
      } else {
          reset();
      }
      
        
    }
    
    /**
     * resets entire bracket
     */
    private void reset(){
        if(confirmReset()){
            //horrible hack to reset
            selectedBracket=new Bracket(startingBracket);
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        }
    }
    
    private void finalizeBracket(){
       if(bracketPane.isComplete()){
           btoolBar.setDisable(true);
           bracketPane.setDisable(true);
           simulate.setDisable(false);
           login.setDisable(false);
           root.setTop(toolBar);
           //save the bracket along with account info
           seralizeBracket(selectedBracket);
            
       }else{
            infoAlert("You can only finalize a bracket once it has been completed.");
            //go back to bracket section selection screen
            // bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        
       }
       //bracketPane=new BracketPane(selectedBracket);
      
      
        
    }
    
    
    /**
     * displays element in the center of the screen
     * 
     * @param p must use a subclass of Pane for layout. 
     * to be properly center aligned in  the parent node
     */
    private void displayPane(Node p){
        root.setCenter(p);
        BorderPane.setAlignment(p,Pos.CENTER);
    }
    
    /**
     * Creates toolBar and buttons.
     * adds buttons to the toolbar and saves global references to them
     */
    private void CreateToolBars(){
        toolBar  = new ToolBar();
        btoolBar  = new ToolBar();
        login=new Button("Log Out");
        simulate=new Button("Simulate");
        scoreBoardButton=new Button("ScoreBoard");
        viewBracketButton= new Button("View Simulated Bracket");
        clearButton=new Button("Clear");
        resetButton=new Button("Reset");
        finalizeButton=new Button("Finalize");
        instructionButton = new Button("Instructions");
        toolBar.getItems().addAll(
                createSpacer(),
                login,
                simulate,
                scoreBoardButton,
                viewBracketButton,
                instructionButton,
                createSpacer()
        );
        btoolBar.getItems().addAll(
                createSpacer(),
                clearButton,
                resetButton,
                finalizeButton,
                back=new Button("Choose Division"),
                instructionButton,
                createSpacer()
        );
    }
    
   /**
    * sets the actions for each button
    */
    private void setActions(){
        login.setOnAction(e->login());
        simulate.setOnAction(e->simulate());
        scoreBoardButton.setOnAction(e->scoreBoard());
        viewBracketButton.setOnAction(e->viewBracket());
        clearButton.setOnAction(e->clear());
        resetButton.setOnAction(e->reset());
        finalizeButton.setOnAction(e->finalizeBracket());
        back.setOnAction(e->{
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        });
    }
    
    /**
     * Creates a spacer for centering buttons in a ToolBar
     */
    private Pane createSpacer(){
        Pane spacer = new Pane();
        HBox.setHgrow(
                spacer,
                Priority.SOMETIMES
        );
        return spacer;
    }
    
    
    private GridPane createLogin(){
        
        
        /*
        LoginPane
        Sergio and Joao
         */

        GridPane loginPane = new GridPane();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(5, 5, 5, 5));

        Text welcomeMessage = new Text("March Madness Login Welcome");
        loginPane.add(welcomeMessage, 0, 0, 2, 1);

        Label userName = new Label("User Name: ");
        loginPane.add(userName, 0, 1);

        TextField enterUser = new TextField();
        loginPane.add(enterUser, 1, 1);

        Label password = new Label("Password: ");
        loginPane.add(password, 0, 2);

        PasswordField passwordField = new PasswordField();
        loginPane.add(passwordField, 1, 2);

        Button signButton = new Button("Sign in");
        loginPane.add(signButton, 1, 4);
        signButton.setDefaultButton(true);//added by matt 5/7, lets you use sign in button by pressing enter

        Label message = new Label();
        loginPane.add(message, 1, 5);

        signButton.setOnAction(event -> {

            // the name user enter
            String name = enterUser.getText();
            // the password user enter
            String playerPass = passwordField.getText();

        
          
            
            if (playerMap.get(name) != null) {
                //check password of user
                 
                Bracket tmpBracket = this.playerMap.get(name);
               
                String password1 = tmpBracket.getPassword();

                if (Objects.equals(password1, playerPass)) {
                    // load bracket
                    selectedBracket=playerMap.get(name);
                    chooseBracket();
                    if(selectedBracket.isComplete()){
                        finalizeBracket();
                    }
                }else{
                   infoAlert("The password you have entered is incorrect!");
                }

            } else {
                //Firas Fares
                //I update my code so we are able to ask user if he want to create a user name or he enter his usr or pass wrong
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setHeaderText(null);
                alert.setContentText("are you sure you want to create another account?");

                ButtonType CreateAccount = new ButtonType("Create Account");
                ButtonType CancelButton = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(CreateAccount,CancelButton);

                Optional<ButtonType> result = alert.showAndWait();
                
                //check for empty fields
                if(!name.equals("")&&!playerPass.equals("")){
                    //create new bracket
                    Bracket tmpPlayerBracket = new Bracket(startingBracket, name);
                    playerBrackets.add(tmpPlayerBracket);
                    tmpPlayerBracket.setPassword(playerPass);

                    playerMap.put(name, tmpPlayerBracket);
                    selectedBracket = tmpPlayerBracket;
                    //alert user that an account has been created
                    infoAlert("No user with the Username \""  + name + "\" exists. A new account has been created.");
                    chooseBracket();
                }
            }
        });
        
        return loginPane;
    }
    
    /**
     * addAllToMap
     * adds all the brackets to the map for login
     */
    private void addAllToMap(){
        for(Bracket b:playerBrackets){
            playerMap.put(b.getPlayerName(), b);   
        }
    }
    
    /**
     * The Exception handler
     * Displays a error message to the user
     * and if the error is bad enough closes the program
     * @param msg message to be displayed to the user
     * @param fatal true if the program should exit. false otherwise 
     */
    private void showError(Exception e,boolean fatal){
        String msg=e.getMessage();
        if(fatal){
            msg=msg+" \n\nthe program will now close";
            //e.printStackTrace();
        }
        Alert alert = new Alert(AlertType.ERROR,msg);
        alert.setResizable(true);
        alert.getDialogPane().setMinWidth(420);   
        alert.setTitle("Error");
        alert.setHeaderText("something went wrong");
        alert.showAndWait();
        if(fatal){ 
            System.exit(666);
        }   
    }
    
    /**
     * alerts user to the result of their actions in the login pane 
     * @param msg the message to be displayed to the user
     */
    private void infoAlert(String msg){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    /**
     * Prompts the user to confirm that they want
     * to clear all predictions from their bracket
     * @return true if the yes button clicked, false otherwise
     */
    private boolean confirmReset(){
        Alert alert = new Alert(AlertType.CONFIRMATION, 
                "Are you sure you want to reset the ENTIRE bracket?", 
                ButtonType.YES,  ButtonType.CANCEL);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult()==ButtonType.YES;
    }
    
    
    /**
     * Tayon Watson 5/5
     * seralizedBracket
     * @param B The bracket the is going to be seralized
     */
    private void seralizeBracket(Bracket B){
        FileOutputStream outStream = null;
        ObjectOutputStream out = null;
        try
        {
            outStream = new FileOutputStream("bin/" + B.getPlayerName()+".ser");
            out = new ObjectOutputStream(outStream);
            out.writeObject(B);
            out.close();
        }
        catch(IOException e)
        {
            // Grant osborn 5/6 hopefully this never happens
            showError(new Exception("Error saving bracket \n"+e.getMessage(),e),false);
        }
    }
    /**
     * Tayon Watson 5/5
     * deseralizedBracket
     * @param filename of the seralized bracket file
     * @return deserialized bracket
     */
    private Bracket deseralizeBracket(String filename){
        Bracket bracket = null;
        FileInputStream inStream = null;
        ObjectInputStream in = null;
        try
        {
            inStream = new FileInputStream("bin/"+ filename);
            in = new ObjectInputStream(inStream);
            bracket = (Bracket) in.readObject();
            in.close();
        }catch (IOException | ClassNotFoundException e) {
            // Grant osborn 5/6 hopefully this never happens either
            showError(new Exception("Error loading bracket \n"+e.getMessage(),e),false);
        }
        return bracket;
    }

    /**
     * Tayon Watson 5/5
     * deseralizedBracket
     * @param filename of the seralized bracket file
     * @return deserialized bracket
     */
    private ArrayList<Bracket> loadBrackets()
    {

        ArrayList<Bracket> list=new ArrayList<Bracket>();
        File dir = new File("bin/");
        int plrCount = 0;

        for (final File fileEntry : dir.listFiles()){
            String fileName = fileEntry.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".")+1);

            if (extension.equals("ser")){
                plrCount++;
            }
        }

        for (final File fileEntry : dir.listFiles()){
            String fileName = fileEntry.getName();
            System.out.println(fileName);
            String extension = fileName.substring(fileName.lastIndexOf(".")+1);
            String[] preSets = {"JamesCPU.ser", "MarkCPU.ser", "CalebCPU.ser", "TracyCPU.ser"};

            int CPU_drop = plrCount - 4;
            System.out.println(CPU_drop);
            ArrayList<String> drop = new ArrayList<>();
            for(int i = 0; i < CPU_drop; i++){
                drop.add(preSets[i]);
            }

            if (extension.equals("ser") && !(drop.contains(fileName))){
                System.out.println((drop.contains(fileName)));
                list.add(deseralizeBracket(fileName));
            }
        }
        return list;
    }


}
