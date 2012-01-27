//Die vector Klasse
#include <vector>
//Der sort Algorithmus
#include <algorithm>
//Ausgabestrom cout
#include <iostream>
//kill Funktion
#include <signal.h>
//gettimeofday Funktion
#include <sys/time.h>
#include <stdio.h>

using namespace std;
//----------------------- Table Schemas -----------------------
struct Customer { double discount; char name[30]; unsigned country; };
struct Country { double tax; char name[30]; };
struct Item { double price; char name[30]; };
struct Order { unsigned customer,item,date; double price; };
 
struct Expense { unsigned customer; double amount; };

//-------------------------- Tables ---------------------------
vector<Customer> customers;
vector<Country> countries;
vector<Item> items;
vector<Order> orders;

//-------------------- Execution parameters ------------------- 
static const unsigned totalTransactions = 1000000;
static const unsigned olapEvery = 10000;

static const unsigned numCustomers = 100000;
static const unsigned numCountries = 100;
static const unsigned numItems = 100000;
static const unsigned numOrders = 1000000;

//Fills tables with random data.
//The number of inserted rows must correspond to the parameter values
void populateDB(unsigned numCountries, unsigned numCustomers, unsigned numItems, unsigned numOrders){
   //TODO Your job
   srand(time(0));
   for(int i=0; i<numCountries; i++){
      Country* x = new Country;
      x->tax = (rand()%50)/100.0;
      //c->name = "Country1";
      sprintf(x->name, "Country %d", i);
      countries.push_back(*x);
   }
   for(int i=0; i<numCustomers; i++){
      Customer* x = new Customer;
      x->discount = (rand()%80)/100;
      //c->name = "Name1";
      sprintf(x->name, "Customer %d", i);
      x->country = rand()%numCountries;
      customers.push_back(*x);
   }
   for(int i=0; i<numItems; i++){
      Item* x = new Item;
      x->price = rand()%5000;
      //i->name = "Item1";
      sprintf(x->name, "Item %d", i);
      items.push_back(*x);
   }
   for(int i=0; i<numOrders; i++){
      Order* x = new Order;
      x->customer = rand()%numCustomers;
      x->item = rand()%numItems;
      x->date = time(NULL)-(rand()%(60*60*24*365));
      x->price = items[x->item].price*(1.0+countries[customers[x->customer].country].tax)*(1.0-customers[x->customer].discount);
      orders.push_back(*x);
   }
}

bool cmp(Expense a, Expense b){
   return a.amount>b.amount;
}

//Performs an OLAP query
void olap(){
   //TODO Your job
   vector<Expense> expenses;
   for(int i=0; i<customers.size(); i++){
      Expense* x = new Expense;
      x->customer = i;
      x->amount = 0.0;
      expenses.push_back(*x);
   }
   for(int i=0; i<orders.size(); i++){
      expenses[orders[i].customer].amount += orders[i].price;
   }
   sort(expenses.begin(), expenses.end(), cmp);
   cout << "--- olap: top ten customers ---" << endl;
   for(int i=0; i<10; i++){
      cout << customers[expenses[i].customer].name << ": " << expenses[i].amount << endl;
   }
}

//Performs an oltp transaction
void oltp(){
   //TODO Your job
   Order* o = new Order();
   o->customer = (rand() % customers.size());
   o->item = (rand() % items.size());
   o->date = time(0);
   o->price = items[o->item].price * (1.0 + countries[customers[o->customer].country].tax) * (1.0 - customers[o->customer].discount);
   orders.push_back(*o);
}

//------------------------ Functions --------------------------
int main()
{
   //Fill database with entries
   populateDB(numCountries,numCustomers,numItems,numOrders);
   //1.) Fork to create an oltp and an olap process.
   int pid = fork();
   //2.) Run olap function in olap process repeatedly
   if(pid == 0){
      while(true){
         olap();
      }
   //3.) Run oltp function in oltp process until numTransaction is reached
   }else if(pid > 0){
      for(int i=0; i<totalTransactions; i++){
         oltp();
      }
   //4.) Once oltp process is finished, also finish olap process (e.g. terminate)
      kill(pid, SIGKILL);
   }else{
      cout << "Error during fork()" << endl;
      exit(0);
   }
   //TODO Your job

  
}

