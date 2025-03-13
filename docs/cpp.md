## How to integrate Tyrico with a cpp or c application

1. Include the tyrico header file 
```#include "tyrico.h"```

2. Define your data queue library, input queue name, and output queue name
```
#define DTAQ_IN  "INPUT     "         // Data queue name
#define DTAQ_OUT "OUTPUT    "       // Data queue name
#define DTAQ_LIB "MYLIB     "    // Data queue library
```

3. Use the imported read and write data queue functions. Message is the message to be run through your 
python model and key will store the key that your data will be stored with in your output data queue.
```
    Key key;
    int res = writeDataQueue(DTAQ_IN, DTAQ_LIB, message, &key);
```

Then to retrieve the output value, use the readDataQueue function with your returned key. Output will
store your return value.
```
    Varchar1000 output;
    int result = readDataQueue(DTAQ_OUT, DTAQ_LIB, key.data, &output);
```