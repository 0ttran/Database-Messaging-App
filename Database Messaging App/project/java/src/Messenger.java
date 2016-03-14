/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.*;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
   if(outputHeader){
      for(int i = 1; i <= numCol; i++){
    System.out.print(rsmd.getColumnName(i) + "\t");
      }
      System.out.println();
      outputHeader = false;
   }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
  Statement stmt = this._connection.createStatement ();
  
  ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
  if (rs.next())
    return rs.getInt(1);
  return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end trye
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Messenger (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("3. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 3: keepon = false; break;
               default : System.out.println("Please enter a number from 1-3. Thank you"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
                mainMenu();
                boolean canDelete = false;
                while(usermenu){
                switch (readChoice()){
                   case 0: mainMenu(); break;
                   case 1: AddToContact(esql, authorisedUser); break;
                   case 2: AddToBlocked(esql, authorisedUser); break;
                   case 3: ListContacts(esql, authorisedUser); break;
                   case 4: ListBlocked(esql, authorisedUser); break;
                   case 5: BrowseChats(esql, authorisedUser); break;
                   case 6: BrowseMessages(esql, authorisedUser); break;
                   case 7: CreateChat(esql, authorisedUser); break;
                   case 8: AddtoChat(esql, authorisedUser); break;
                   case 9: NewMessage(esql, authorisedUser); break;
                   case 10: EditMessage(esql, authorisedUser); break;
                   case 11: DeleteMessage(esql, authorisedUser); break;
                   case 12: DeleteContact(esql, authorisedUser); break;
                   case 13: DeleteBlocked(esql, authorisedUser); break;
                   case 14: DeleteChat(esql, authorisedUser); break;
                   case 15: canDelete = DeleteAccount(esql, authorisedUser);
                        if(canDelete == true)
                        {
                                usermenu = false;
                                 break;
                        }
                        else
                            break;
                   case 16: usermenu = false; break;
                   case 17: usermenu = false; keepon = false; break;
                   default : System.out.println("Please enter a number from 0-15. Thank you"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main
  
   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface                       \n" +
         "*******************************************************\n");
   }//end Greeting
public static void mainMenu() {
        System.out.println("MAIN MENU");
        System.out.println("---------");
        System.out.println("0. Redisplay Main Menu");
        System.out.println("1. Add to contact list");
        System.out.println("2. Add to blocked list");
        System.out.println("3. Browse contact list");
        System.out.println("4. Browse blocked list");
        System.out.println("5. Browse chats");
        System.out.println("6. Browse messages");
        System.out.println("7. Start new chat");
        System.out.println("8. Add to chat"); //new
        System.out.println("9. New Message");
        System.out.println("10. Edit Message");
        System.out.println("11. Delete Message");
        System.out.println("12. Delete user from contacts list");
        System.out.println("13. Delete user from blocked list");
        System.out.println("14. Delete chat");
        System.out.println("15. Delete Account");
        System.out.println(".........................");
        System.out.println("16. Log out");
        System.out.println("17. Exit program");
}

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
 System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

        //check if user exists
        String q2 = String.format("SELECT * FROM USR WHERE login = '%s'", login);
        int chkUsr = esql.executeQuery(q2);

        //if user is not taken
        if(chkUsr > 0){
                //Creating empty contact\block lists for a user     
                esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
                int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
                esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
                int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");

                String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);

                esql.executeUpdate(query);
                System.out.println ("User successfully created!");
        }
        else{
                System.out.println("User is already taken");
        }

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
        
         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
   if (userNum > 0)
    return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end
   

   public static void AddToContact(Messenger esql, String currUser){
       try{
           System.out.print("Enter the login name of the user: ");
           String contact = in.readLine();

           if(currUser.equals(contact)){
               System.out.print("\n---------------------\nYou cannot add yourself!\n---------------------\n\n");
               return;
           }

           //Check if the user is in database
           String q = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
           int userNum = esql.executeQuery(q);

           //If user is in database, then add to the current user's contact list 
           if(userNum > 0){
                //check if user in the block list
               String q5 = String.format("SELECT list_member FROM USR, USER_LIST_CONTAINS WHERE list_member = '%s' AND block_list = list_id",contact);
               int chkIfBlk = esql.executeQuery(q5);
                //check if user is in the contact list already
               String q6 = String.format("SELECT list_member FROM USR, USER_LIST_CONTAINS WHERE list_member = '%s' AND contact_list = list_id", contact);
               int chkIfContact = esql.executeQuery(q6);

                if(chkIfBlk > 0){
                   System.out.println("\n----------------------------------------\n");
                   System.out.println("This person is on your blocked list, please delete this person off your block list to add");
                   System.out.println("\n----------------------------------------\n");
                }
               else if(chkIfContact > 0){
                        System.out.println("\n----------------------------------------\n");
                        System.out.println("This person is in your contact list. Cannot add.");
                        System.out.println("\n----------------------------------------\n");
                }
               else{
                   String q2 = String.format("SELECT contact_list FROM USR WHERE login = '%s'", currUser);
                   List<List<String>> idQuery = esql.executeQueryAndReturnResult(q2);
                   String ID = "";
                   for(String s : idQuery.get(0)){
                       ID += s + "\t";
                   }
                   //System.out.print("Contactid: " + ID + "\n");//ID.substring(1, ID.length()-1) + "\n");
                   String q3 = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES (%s,'%s')",ID, contact);
                   esql.executeUpdate(q3);
                   
                   System.out.print("\n----------------------------------------\n");
                   System.out.println("User successfully added!");
                   System.out.print("----------------------------------------\n");
               }
           }
           else{
               System.out.print("\n----------------------------------------\n");
               System.out.println("User does not exist!");
               System.out.print("----------------------------------------\n");
           }
       }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
       
   }//end
   
   public static void AddToBlocked(Messenger esql, String currUser){
       try{
           System.out.print("Enter the login name of the user: ");
           String contact = in.readLine();
           
           if(currUser.equals(contact)){
               System.out.print("\n---------------------\nYou cannot block yourself!\n---------------------\n\n");
               return;
           }
           //Check if the user is in database
           String q = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
           int userNum = esql.executeQuery(q);
    
            
           //If user is in database, then add to the current user's blocked list 
           if(userNum > 0){
                //check if contact is in contacts
                String q4 = String.format("SELECT list_member FROM USR, USER_LIST_CONTAINS WHERE list_member = '%s' AND contact_list = list_id", contact);
                int chkContact = esql.executeQuery(q4);

                String q5 = String.format("SELECT list_member FROM USR, USER_LIST_CONTAINS WHERE list_member = '%s' AND block_list = list_id", contact);
                int chkBlk = esql.executeQuery(q5);

                //if it is in contact already, delete from contact and then add to blocked
                if(chkContact > 0)
                {
                        String q6 = String.format("SELECT contact_List FROM USR WHERE login = '%s'", currUser);
                        List<List<String>> idQuery = esql.executeQueryAndReturnResult(q6);
                        String ID = "";
                        for(String s : idQuery.get(0)){
                                ID += s + "\t";
                        }

                        String q7 = String.format("SELECT list_member FROM USER_LIST_CONTAINS WHERE list_id = %s AND list_member = '%s'", ID, contact);
                        int checkIfInList = esql.executeQuery(q7);

                        if(checkIfInList > 0){
                                String q8 = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = %s AND list_member = '%s'", ID, contact);
                                esql.executeUpdate(q8);
                        }

                        String q9 = String.format("SELECT block_List FROM USR WHERE login = '%s'", currUser);
                        List<List<String>> idQuery2 = esql.executeQueryAndReturnResult(q9);
                        String ID2 = "";
                        for(String s : idQuery2.get(0)){
                                ID2 += s + "\t";
                        }

                        String q10 = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES (%s,'%s')",ID2, contact);
                        esql.executeUpdate(q10);

                        System.out.print("\n----------------------------------------\n");
                        System.out.println("User successfully added!");
                        System.out.print("----------------------------------------\n");
                }
                else if(chkBlk > 0)
                {
                        System.out.println("\n----------------------------------------\n");
                        System.out.println("This contact is already on your blocked list");
                        System.out.println("\n----------------------------------------\n");
                }
    else{
                String q2 = String.format("SELECT block_List FROM USR WHERE login = '%s'", currUser);
                List<List<String>> idQuery = esql.executeQueryAndReturnResult(q2);
                String ID = "";
                for(String s : idQuery.get(0)){
                    ID += s + "\t";
                }
            
               String q3 = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES (%s,'%s')",ID, contact);
               esql.executeUpdate(q3);
               
               System.out.print("\n----------------------------------------\n");
               System.out.println("User successfully added!");
               System.out.print("----------------------------------------\n");
    }
           }
           else{
               System.out.print("\n----------------------------------------\n");
               System.out.println("User does not exist!");
               System.out.print("----------------------------------------\n");
           }
       }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
        }
   }
   
   public static void ListContacts(Messenger esql, String currUser){
       try{
           System.out.print("\n----------------------------------------\n");
           //Gets current user contact id
           String q2 = String.format("SELECT contact_list FROM USR WHERE login = '%s'", currUser);
           List<List<String>> idQuery = esql.executeQueryAndReturnResult(q2);
           String ID = "";
           for(String s : idQuery.get(0)){
               ID += s + "\t";
           }
           
           //Select contacts with current user id
           String q = String.format("SELECT list_member as Contacts, status FROM USER_LIST_CONTAINS, USR WHERE list_id = %s and login = '%s'", ID, currUser);
           esql.executeQueryAndPrintResult(q); 
           System.out.print("----------------------------------------\n");
       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }
   }//end
   
   public static void ListBlocked(Messenger esql, String currUser){
       try{
           System.out.print("\n----------------------------------------\n");
           //Gets current user contact id
           String q2 = String.format("SELECT block_list FROM USR WHERE login = '%s'", currUser);
           List<List<String>> idQuery = esql.executeQueryAndReturnResult(q2);
           String ID = "";
           for(String s : idQuery.get(0)){
               ID += s + "\t";
           }
           
           //Select blocked with current user id
           String q = String.format("SELECT list_member as blocked FROM USER_LIST_CONTAINS WHERE list_id = %s", ID);
           esql.executeQueryAndPrintResult(q); 
           System.out.print("----------------------------------------\n");
       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }
   }
   
   public static void NewMessage(Messenger esql, String currUser){
       try{
           System.out.print("Enter a chat ID to send a message to: ");
           String chatID = in.readLine();

           //Check if the user is apart of the chatID 
           String q = String.format("SELECT * FROM CHAT_LIST WHERE chat_id=%s AND member='%s'", chatID, currUser);
           int chkUser = esql.executeQuery(q);

           if(chkUser > 0){
               System.out.print("Enter your message: ");
               String msg = in.readLine();

               //int MsgID = esql.getCurrSeqVal("Message_msg_id_seq"); //Message_msg_id_seq????
               //System.out.println("Your message ID is: " + MsgID + "\n");

               String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z").format(new Date());
               System.out.println("msg: " + msg + " time: " +  timeStamp + " currUser: " + currUser + " chatID: " + chatID);
               String q2 = String.format("INSERT INTO MESSAGE (msg_text, msg_timestamp, sender_login, chat_id) VALUES ('%s', '%s', '%s', %s)", msg, timeStamp, currUser, chatID);
               esql.executeUpdate(q2);
           }
           else{
               System.out.print("You are not apart of this chat!\n");
           }
       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }

   }
   
   public static void addUserToChat(Messenger esql, String currUser, int chatID){
       try{
           
           //Check if user is the inital sender
           String q = String.format("SELECT * FROM CHAT WHERE chat_id=%s AND init_sender='%s'", chatID, currUser);
           int chk = esql.executeQuery(q);
           
           if(chk > 0){
               System.out.print("Enter in user (login name) that you want to add: ");
               String userToAdd = in.readLine();
               
               //Check if user in db
               String q2 = String.format("SELECT * FROM Usr WHERE login = '%s'", userToAdd);
               int chkUser = esql.executeQuery(q2);
               
               if(chkUser > 0){
                   String q3 = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES (%s, '%s')", chatID, userToAdd);
                   esql.executeUpdate(q3);
                   System.out.print("User added to chat list!\n");
               }
               else{
                   System.out.print("Invalid User!\n");
               }
           }
           else{
               System.out.println("Invalid chat ID or you are not the initial sender of this chat");
           }
           
       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }
   }
   
   public static void CreateChat(Messenger esql, String currUser){       
       try{
           int numPeople = 0;

           String q1 = String.format("INSERT INTO CHAT(chat_type, init_sender) VALUES ('private', '%s')", currUser); 
           esql.executeUpdate(q1);
           int chatid = esql.getCurrSeqVal("chat_chat_id_seq"); //chat_chat_id_seq;
           System.out.print("Your chat ID is: " + chatid + "\n");
           
           String q2 = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES (%d, '%s')", chatid, currUser);
           esql.executeUpdate(q2);
           
           boolean flg = true; 
           while(flg){
               addUserToChat(esql, currUser, chatid);
               numPeople++;
               if(numPeople == 2){
                   String q3 = String.format("UPDATE CHAT SET chat_type='group' WHERE chat_id=%s", chatid);
                   esql.executeUpdate(q3);
               }
               System.out.print("Are you done adding? ('y' or 'n'): ");
               String yn = in.readLine();
               if(yn.equals("y")){
                   flg = false;
               }
           }
           

       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }
       
   }//end 

   public static void BrowseChats(Messenger esql, String currUser){
       try{
           String q1 = String.format("SELECT chat_id as chats FROM CHAT_LIST WHERE member = '%s'", currUser);
           esql.executeQueryAndPrintResult(q1);
        
       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }
   }
   
   public static void EditMessage(Messenger esql, String currUser){ 
        try{
                //get the message Id that user wants to delete
                System.out.println("Choose the message id you like to edit: ");
                String msgID = in.readLine();

                //check if message id exists
                String q = String.format("SELECT * FROM MESSAGE WHERE msg_id = %s",msgID);
                int chkMsg = esql.executeQuery(q);

                //if chat id is valid
                if(chkMsg > 0)
                {
                         //check if message is the sender
                        String q2 = String.format("SELECT * FROM MESSAGE WHERE sender_login = '%s'", currUser);
                        int chkUser = esql.executeQuery(q2);

                        //if user is valid
                        if(chkUser > 0){
                                System.out.println("What would you like to update the message to? ");
                                String newMsg = in.readLine();
                                //select the message
                                String q3 = String.format("UPDATE MESSAGE SET msg_text = '%s' WHERE msg_id = %s",newMsg,msgID);
                                esql.executeUpdate(q3);
                        }
                        else{
                                System.out.println("You are not the sender of this message!\n");
                        }
                }
                else{
                        System.out.println("This message does not exist!\n");
                }
        }catch(Exception e){
                System.err.println(e.getMessage());
                return;
        }
   }
   
   public static void BrowseMessages(Messenger esql, String currUser){

         try{
           
           System.out.print("Enter a chat ID that you want to view messages of: ");
           String chatid = in.readLine();
           
            //Check if the user is apart of the chatID 
           String q3 = String.format("SELECT * FROM CHAT_LIST WHERE chat_id=%s AND member='%s'", chatid, currUser);
           int chkUser = esql.executeQuery(q3);
           
           if(chkUser>0){
               String q = String.format("SELECT msg_timestamp, msg_id, sender_login, msg_text AS messages FROM  MESSAGE WHERE chat_id=%s ORDER BY msg_timestamp DESC", chatid);
               List<List<String>> listMsg = esql.executeQueryAndReturnResult(q);
                
               int numMsg = 0, num = 0;
               boolean done = true;
               
               while(done){
                   //numMsg += 10;
                   if((listMsg.size() - numMsg) < 10){
                       num = listMsg.size();
                       //numMsg = 0;
                   }
                    else{
                        num = 10 + numMsg;                    
                    }
                    
                   for(int i = numMsg; i < num; i++){
                       System.out.println("Msg ID: " + listMsg.get(i).get(1)); //Msg id
                       System.out.println(listMsg.get(i).get(0)); //Timestamp
                       System.out.println(listMsg.get(i).get(2)); //Username
                       System.out.println(listMsg.get(i).get(3)); //Message text
                   }
                   
                   if((listMsg.size() - (10+numMsg)) > 0){
                       System.out.println("Do you want to load earlier messages? ('y' or 'n')");
                       String ans = in.readLine();
                       
                       if(ans.equals("n")){
                           System.out.println("No more messages to display!");
                           done = false;
                       }
                       //num = listMsg.size() - 10  + numMsg;
                   }
                   else{
                       //System.out.println("No more messages to display!");
                        done = false;
                    }
                    numMsg += 10;
               }
           }
           else{
               System.out.println("Chat ID does not exist or you do not belong to this chat.");
           }
       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }
   }

   public static void DeleteContact(Messenger esql, String currUser){
       try{
           System.out.print("Enter the login name of the user: ");
           String contact = in.readLine();

           //Check if the user is in database
           String q = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
           int userNum = esql.executeQuery(q);
            
           //If user is in database, then delete them from
           if(userNum > 0){
               String q2 = String.format("SELECT contact_List FROM USR WHERE login = '%s'", currUser);
               List<List<String>> idQuery = esql.executeQueryAndReturnResult(q2);
               String ID = "";
               for(String s : idQuery.get(0)){
                   ID += s + "\t";
               }
               
               String q4 = String.format("SELECT list_member FROM USER_LIST_CONTAINS WHERE list_id = %s AND list_member = '%s'", ID, contact);
               int checkIfInList = esql.executeQuery(q4);
               
               if(checkIfInList > 0){
                   String q3 = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = %s AND list_member = '%s'", ID, contact);
                   esql.executeUpdate(q3);
                   System.out.print("\n----------------------------------------\n");
                   System.out.println("User successfully deleted from contacts list!");
                   System.out.print("----------------------------------------\n");
               }
               else{
                System.out.print("\n----------------------------------------\n");
                System.out.println("User not found in contacts list!");
                System.out.print("----------------------------------------\n");
            }
           }
       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }
   }

   public static void DeleteBlocked(Messenger esql, String currUser){
       try{
           System.out.print("Enter the login name of the user: ");
           String contact = in.readLine();

           //Check if the user is in database
           String q = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
           int userNum = esql.executeQuery(q);
            
           //If user is in database, then delete them from
           if(userNum > 0){
               String q2 = String.format("SELECT block_List FROM USR WHERE login = '%s'", currUser);
               List<List<String>> idQuery = esql.executeQueryAndReturnResult(q2);
               String ID = "";
               for(String s : idQuery.get(0)){
                   ID += s + "\t";
               }
               
               String q4 = String.format("SELECT list_member FROM USER_LIST_CONTAINS WHERE list_id = %s AND list_member = '%s'", ID, contact);
               int checkIfInList = esql.executeQuery(q4);
               
               if(checkIfInList > 0){
                   String q3 = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = %s AND list_member = '%s'", ID, contact);
                   esql.executeUpdate(q3);
                   System.out.print("\n----------------------------------------\n");
                   System.out.println("User successfully deleted from blocked list!");
                   System.out.print("----------------------------------------\n");
               }
               else{
                System.out.print("\n----------------------------------------\n");
                System.out.println("User not found in blocked list!");
                System.out.print("----------------------------------------\n");
            }
           }
       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }
   }
   
   public static void DeleteChat(Messenger esql, String currUser){
       try{
           System.out.print("Enter the ID of the chat to delete: ");
           String chatID = in.readLine();
           
           //Check if the current user is the one that made the chat
           String q = String.format("SELECT * FROM CHAT WHERE chat_id=%s AND init_sender='%s'", chatID, currUser);
           int chk = esql.executeQuery(q);
           
           //Deletes chat, chatlist and messages
           if(chk > 0){
               String q1 = String.format("DELETE FROM CHAT WHERE chat_id=%s", chatID);
               String q2 = String.format("DELETE FROM CHAT_LIST WHERE chat_id=%s", chatID);
               String q3 = String.format("DELETE FROM MESSAGE WHERE chat_id=%s", chatID);
               
               esql.executeUpdate(q3);
               esql.executeUpdate(q2);
               esql.executeUpdate(q1);
           }
           else{
               System.out.print("You cannot delete this chat.\n");
           }
        
       }catch(Exception e){
           System.err.println(e.getMessage());
           return;
       }
   }
   
   public static boolean DeleteAccount(Messenger esql, String currUser){
         try{
                // //check if there are still chat manager
                boolean chatEmpty = false;
                String q2 = String.format("SELECT *FROM CHAT WHERE init_sender = '%s'", currUser);
                int userNum = esql.executeQuery(q2);

                if(userNum == 0)
                        chatEmpty = true;

                //if there is no chat manager, then delete the account
                if(chatEmpty)
                {
                        String q = String.format("DELETE FROM USR WHERE login = '%s'",currUser);
                        String q3 = String.format("DELETE FROM CHAT_LIST WHERE member = '%s'", currUser);
                        String q4 = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = '%s'", currUser);
                        esql.executeUpdate(q3);
                        esql.executeUpdate(q4);
                        esql.executeUpdate(q);
                        System.out.println("User is deleted!");
                        return true;
                }
                //otherwise
                else
                {
                        System.out.println("There are still chats not deleted. Please delete them first.");
                        //list the chats still active
                        String q5 = String.format("SELECT chat_id as chats FROM CHAT WHERE init_sender = '%s'", currUser);
                        esql.executeQueryAndPrintResult(q5);
                        return false;
                }
        }catch(Exception e){
                System.err.println(e.getMessage());
                return false;
        }

   }

public static void DeleteMessage(Messenger esql, String currUser){
        try{
                //get the message Id that user wants to delete
                System.out.println("Choose the message id you like to delete: ");
                String msgID = in.readLine();

                //check if message id exists
                String q = String.format("SELECT * FROM MESSAGE WHERE msg_id = %s",msgID);
                int chkMsg = esql.executeQuery(q);

                //if chat id is valid
                if(chkMsg > 0)
                {
                         //check if message is the sender
                        String q2 = String.format("SELECT * FROM MESSAGE WHERE sender_login = '%s'", currUser);
                        int chkUser = esql.executeQuery(q2);

                        //if user is valid
                        if(chkUser > 0){
                                String q3 = String.format("DELETE FROM MESSAGE WHERE msg_id=%s", msgID);
                                esql.executeUpdate(q3);
                                System.out.println("Message has been deleted.");
                        }
                        else{
                                System.out.println("You are not the sender of this message!\n");
                        }
                }
                else{
                        System.out.println("This message does not exist\n");
                }
        }catch(Exception e){
                System.err.println(e.getMessage());
                return;
        }
}

public static void AddtoChat(Messenger esql, String currUser){
        try{
                //get the chatID that wants to be added
                System.out.println("Enter the chatID: ");
                String chatID = in.readLine();

                //check if chatID is real
                String q = String.format("SELECT * FROM CHAT_LIST WHERE chat_id = %s", chatID);
                int chkChat = esql.executeQuery(q);

                if(chkChat > 0){
                        addUserToChat(esql, currUser, Integer.parseInt(chatID));
                }
                else{
                        System.out.println("This chat does not exist!");
                }
        }catch(Exception e){
                System.err.println(e.getMessage());
                return;
        }
}

}//end Messenger
