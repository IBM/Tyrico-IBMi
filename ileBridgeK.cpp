#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <qsnddtaq.h>
#include <qrcvdtaq.h>

#include <bcd.h>
#include <ctime>
#include <string>
#include <sstream>

#define DTAQ_IN "D16        "         // Data queue name
#define DTAQ_OUT "D18KEYED   "       // Data queue name
#define DTAQ_LIB "JONATHAN  "    // Data queue library
#define MESSAGE_LENGTH 100       // Message length
#define MESSAGE_FORMAT "Message %d" // Message format
#define COUNTER_LENGTH 7 // Number of bytes in counter

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


template <typename T>
string to_string_custom(T value) {
    ostringstream oss;
    oss << value;
    return oss.str();
}

string readDataQueue(char* dtaq_name, char* library_name, string key){
    char _out[1024];
    decimal(5,0) returned_len = 0;
    int key_size = 17;
    char keybuf[key_size] __attribute__((aligned(16)));
    memset(keybuf, ' ', key_size);
    memcpy(keybuf, key.c_str(), strlen(key.c_str()));
    decimal(5,0) timeout = 1;
    _DecimalT<5,0> len = __D("0");

    QRCVDTAQ(
      // 1 	Data queue name 	Input 	Char(10)
      dtaq_name,
      // 2 	Library name 	Input 	Char(10)
      library_name,
      // 3 	Length of data 	Output 	Packed(5,0)
      &returned_len, 
      // 4 	Data 	Output 	Char(*)
      _out,
      // 5 	Wait time 	Input 	Packed(5,0)
      (decimal(5, 0)) - 1,
      //   Optional Parameter Group 1:
      // 6 	Key order 	Input 	Char(2)
      "EQ",
      // 7 	Length of key data 	Input 	Packed(3,0)
      (decimal(3, 0))key_size,
      // 8 	Key data 	I/O 	Char(*)
      (void *)keybuf,
      // 9 	Length of sender information 	Input 	Packed(3,0)
      (decimal(3, 0))0,
      // 10 	Sender information 	Output 	Char(*)
      0);

    return string(_out);
}

void writeDataQueue(char* dtaq_name, char* dtaq_lib, char* message){

        _DecimalT<5,0> len = __D("0");
        len += strlen(message);
        printf("Sending message %s\n", message);
        // Call the QSNDDTAQ API
        QSNDDTAQ(dtaq_name, dtaq_lib, len, (void *)message);
}

string getKey(time_t* lastResetTime, int* counter ){
        // Gets the key, and modifies the counter and lastResetTime
        time_t timestampSeconds = time(0);
        if (timestampSeconds  - *lastResetTime >= 1) {
            *counter = 0;
            *lastResetTime = timestampSeconds;
        } else {
            *counter = *counter + 1;
        }

        // Convert time_t to string. This is 10 characters long for the next two centuries.
        string timeStr = to_string_custom(timestampSeconds);
        
        // Convert int to string
        string numStr = to_string_custom(*counter);

        // Pad with leading zeros to ensure 6 characters
        if (numStr.length() < COUNTER_LENGTH) {
            numStr.insert(0, COUNTER_LENGTH - numStr.length(), '0');
        }

        // Concatenate the strings. This is 15 characters long.
        string key = timeStr + numStr;
        return key;
}


int main() {
    char message[MESSAGE_LENGTH]; // Buffer for the message
    const int num_messages = 1000;
    string keys[num_messages];

    // Loop to send messages 100 times
    time_t lastResetTime = time(0);
    int counter = 0;

    for (int i = 0; i <= num_messages; i++) {
        string key = getKey(&lastResetTime, &counter);
        string value = flowers[i % data_length];

        // Add key to the array for later use
        keys[i] = key;
       
        // Create the message
        snprintf(message, sizeof(message), "%s%s", key.c_str(), value.c_str());

        writeDataQueue(DTAQ_IN, DTAQ_LIB, message);
    }

    for (int i = 0; i < num_messages; i++){
        string key = keys[i];
        string res = readDataQueue(DTAQ_OUT, DTAQ_LIB, key);
        printf("Received message key %s, value %s\n", key.c_str(), res.c_str());
    }
    
    return 0;
}
