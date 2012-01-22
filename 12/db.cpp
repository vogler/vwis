#include <vector>
#include <iterator>
#include <algorithm>
#include <iostream>
#include <iomanip>
#include <random>
#include <tuple>
#include <cstring>
#include <cstdio>

#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>


using namespace std;


struct Customer
{
    double discount;
    char name[30];
    size_t country;
};

struct Country
{
    double tax;
    char name[30];
};

struct Item
{
    double price;
    char name[30];
};

struct Order
{
    size_t customer;
    size_t item;
    unsigned int date;
    double price;
};


vector<Customer> customers;
vector<Country> countries;
vector<Item> items;
vector<Order> orders;

static const size_t totalTransactions = 1000000;

static const size_t numCustomers = 100000;
static const size_t numCountries = 100;
static const size_t numItems = 100000;
static const size_t numOrders = 1000000;

static default_random_engine rng;


static string randomName(const string &prefix)
{
    uniform_int_distribution<int> randomNo(0, numeric_limits<int>::max());
    return prefix + "_" + to_string(randomNo(rng));
}


static void fillRandomName(const string &prefix, char *dest, size_t size)
{
    string name = randomName(prefix);
    strncpy(dest, name.c_str(), size);
}


static Country randomCountry()
{
    Country c;
    fillRandomName("country", c.name, sizeof(c.name));
    c.tax = uniform_real_distribution<>(0, 0.5)(rng);
    return c;
}


static Customer randomCustomer()
{
    Customer c;
    c.country = uniform_int_distribution<int>(0, countries.size()-1)(rng);
    c.discount = uniform_real_distribution<>(0, 0.5)(rng);
    fillRandomName("cust", c.name, sizeof(c.name));
    return c;
}


static Item randomItem()
{
    Item i;
    fillRandomName("item", i.name, sizeof(i.name));
    i.price = uniform_real_distribution<>(0.1, 100)(rng);
    return i;
}


static double calculatePrice(const Order &order)
{
    auto &customer = customers[order.customer];
    auto &country = countries[customer.country];
    auto &item = items[order.item];
    return item.price * (1.0 * country.tax) * (1.0 - customer.discount);
}


static Order randomOrder()
{
    Order o;
    o.customer = uniform_int_distribution<size_t>(0, customers.size()-1)(rng);
    o.item = uniform_int_distribution<size_t>(0, items.size()-1)(rng);
    o.date = uniform_int_distribution<unsigned int>(
        0, numeric_limits<unsigned int>::max())(rng);
    o.price = calculatePrice(o);
    return o;
}


void populateDB(size_t numCountries, size_t numCustomers, size_t numItems,
                size_t numOrders)
{
    generate_n(back_inserter(countries), numCountries, randomCountry);
    generate_n(back_inserter(customers), numCustomers, randomCustomer);
    generate_n(back_inserter(items), numItems, randomItem);
    generate_n(back_inserter(orders), numOrders, randomOrder);
}


struct CustomerExpense
{
    size_t customer;
    double expense;
};


bool operator>(const CustomerExpense &self, const CustomerExpense &other)
{
    return self.expense > other.expense;
}

ostream &operator<<(ostream &stream, const CustomerExpense &expense)
{
    auto &customer = customers[expense.customer];
    stream << string(customer.name, sizeof(customer.name))
           << ": " << expense.expense;
    return stream;
}


void olap()
{
    vector<CustomerExpense> expenses(customers.size());
    for (size_t c=0; c < customers.size(); ++c)
    {
        expenses[c].customer = c;
    }
    for_each(orders.begin(), orders.end(), [&expenses](const Order &o) {
            expenses[o.customer].expense += o.price;
        });
    sort(expenses.begin(), expenses.end(), greater<CustomerExpense>());
    ostream_iterator<CustomerExpense> sink(cout, "\n");
    copy_n(expenses.begin(), 10, sink);
}


void oltp()
{
    orders.push_back(randomOrder());
}


int main()
{
    populateDB(numCountries, numCustomers, numItems, numOrders);

    pid_t pid = fork();
    if (pid == -1) {
        perror(0);
        return EXIT_FAILURE;
    }
    else if (pid == 0)
    {
        for (;;) { olap(); }
    }
    else
    {
        for (size_t i = 0; i < totalTransactions; ++i)
        {
            oltp();
        }
        kill(pid, SIGTERM);
        // avoid zombies
        wait(0);
    }
}

