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

#define DEBUG 0


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
   srand(time(0));
   for(int i=0; i<numCountries; i++){
      Country* x = new Country;
      x->tax = (rand()%50)/100.0;
      sprintf(x->name, "Country %d", i);
      countries.push_back(*x);
   }
   for(int i=0; i<numCustomers; i++){
      Customer* x = new Customer;
      x->discount = (rand()%80)/100;
      sprintf(x->name, "Customer %d", i);
      x->country = rand()%numCountries;
      customers.push_back(*x);
   }
   for(int i=0; i<numItems; i++){
      Item* x = new Item;
      x->price = rand()%5000;
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

long tdiff(timeval start, timeval end){
   return ((end.tv_sec * 1000000 + end.tv_usec) - (start.tv_sec * 1000000 + start.tv_usec));
}

//Performs an OLAP query
void olap(){
   long TQuery = 0;
   timeval start, end;
   gettimeofday(&start, 0);

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
   gettimeofday(&end, 0);
   if(DEBUG){
      cout << endl << "--- olap: top ten customers ---" << endl;
      for(int i=0; i<10; i++){
         cout << customers[expenses[i].customer].name << ": " << expenses[i].amount << endl;
      }
   }
   TQuery = tdiff(start, end);
   cout << "TQuery: " << TQuery << "µs" << endl;
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
int main(int argc, char *argv[])
{
   int n_olap = 3;
   if(argc == 2){
      n_olap = atoi(argv[1]);
   }
   cout << "N_OLAP: " << n_olap << endl;

   timeval start, end;
   long TFork = 0, TTrans = 0, TPS = 0;

   //Fill database with entries
   populateDB(numCountries,numCustomers,numItems,numOrders);
   //1.) Fork to create an oltp and an olap process.
   vector<int> pids;
   int pid = 1; // for n_olap = 0
   gettimeofday(&start, 0);
   for(int i=0; i<n_olap; i++){
      pid = fork();
      if(pid > 0){
         pids.push_back(pid);
      }else{
         break;
      }
   }
   //2.) Run olap function in olap process repeatedly
   if(pid == 0){
      while(true){
         olap();
      }
   //3.) Run oltp function in oltp process until numTransaction is reached
   }else if(pid > 0){
      gettimeofday(&end, 0);
      TFork = tdiff(start, end);
      
      gettimeofday(&start, 0);
      for(int i=0; i<totalTransactions; i++){
         oltp();
      }
      gettimeofday(&end, 0);
      TTrans = tdiff(start, end);
   //4.) Once oltp process is finished, also finish olap process (e.g. terminate)
      for(int i=0; i<pids.size(); i++){
         kill(pids[i], SIGKILL);
      }
      TPS = 1000000.0 * totalTransactions / TTrans;
      cout << "------------" << endl;
      cout << "N_OLAP: " << n_olap << endl;
      cout << "TFork: " << TFork << "µs" << endl;
      cout << "TTrans: " << TTrans << "µs" << endl;
      cout << "TPS: " << TPS << endl;
      cout << "------------" << endl;
      cout << endl;
   }else{
      cout << "Error during fork()" << endl;
      exit(0);
   }
  
}

