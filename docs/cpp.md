## How to integrate Tyrico with a cpp or c application

1. Include the Tyrico header file from the `libs` directory. Also make sure to copy the file into your project folder.

```clike
#include "tyrico.h"
```

2. Define your data queue library, input queue name, and output queue name

```clike
#define DTAQ_IN  "INPUT"
#define DTAQ_OUT "OUTPUT"
#define DTAQ_LIB "MYLIB"
```

3. Use the imported read and write data queue functions. `message` is the message to be sent to your 
python receiver hosting your AI model. `key` will store the key that your returned output data will be stored with 
in your keyed output data queue.

```clike
Key key;
int res = writeDataQueue(DTAQ_IN, DTAQ_LIB, message, &key);
```

Then to retrieve the output value, use the readDataQueue function with your returned key. `output` will
store your return value. Like `Key`, `Varchar1000` is a custom type that is imported from `tyrico.h`.

```clike
Varchar1000 output;
int result = readDataQueue(DTAQ_OUT, DTAQ_LIB, key.data, &output);
```

4. Here's a full sample application

```clike
#include <string>
#include "../libs/tyrico.h"

#define DTAQ_IN  "DIN"
#define DTAQ_OUT "DOUTKEYED"
#define DTAQ_LIB "JONATHAN"

using namespace std;

string flowers[] = {
"[7.47,2.47,5.34,2.34]",
"[5.12,2.28,6.81,0.39]",
"[7.16,3.70,4.58,1.24]",
"[4.99,2.92,4.22,2.23]",
"[6.09,4.31,2.46,1.96]",
"[6.22,4.29,6.53,1.46]",
"[5.54,2.13,2.74,0.43]",
"[7.46,3.85,4.70,1.51]",
"[7.76,3.43,6.54,0.64]",
"[7.68,3.65,6.63,2.35]",
"[7.03,2.31,6.68,0.78]",
"[6.44,2.68,5.40,2.13]",
"[5.97,2.56,6.14,0.56]",
"[4.56,2.60,3.33,0.79]",
"[4.94,2.94,6.77,0.68]",
"[7.85,2.82,4.00,1.28]",
"[4.66,4.10,2.66,2.31]",
"[7.43,2.85,3.06,0.56]",
"[5.29,2.62,2.28,0.32]",
"[4.70,2.54,1.29,2.42]",
"[5.92,4.22,3.32,0.81]",
"[6.64,3.18,2.29,1.13]",
"[5.25,2.10,3.07,0.47]",
"[6.00,2.44,1.75,2.43]",
"[6.10,2.44,5.30,0.19]",
"[7.13,4.25,5.67,0.74]",
"[6.80,2.37,5.09,0.37]",
"[7.76,2.48,2.66,0.63]"};

int data_length = sizeof(flowers) / sizeof(flowers[0]);

int main() {
    const int num_messages = 100000;
    string keys[num_messages];
    Key key;
    for (int i = 0; i < num_messages; i++) {
        char* message = (char*)(flowers[i % data_length]).c_str();
        printf("sending message %d\n", i);

        int res = writeDataQueue(DTAQ_IN, DTAQ_LIB, message, &key);
        // Add key to the array for later use
        if (res == 0){
            keys[i] = string(key.data);
        } else {
            keys[i] = string("");
        }
    }

    for (int i = 0; i < num_messages; i++){
        string returned_key = keys[i];
        if (returned_key != ""){
            Varchar1000 output;
            int result = readDataQueue(DTAQ_OUT, DTAQ_LIB, (char*)returned_key.c_str(), &output);
            if (result == 0){
                printf("Received message key %s, value %s\n", returned_key.c_str(), output.data);
            } else {
                printf("Got error %d\n", result);
            }
        } 
    }
    
    return 0;
}
```

5. Here is some output from the sample application. The model has classified each iris flower from the flowers
array as 0, 1, or 2 corresponding to different iris flower species.

```
Received message key 17418970560001408, value 2
Received message key 17418970560001409, value 0
Received message key 17418970560001410, value 2
Received message key 17418970560001411, value 1
Received message key 17418970560001412, value 1
Received message key 17418970560001413, value 2
```
