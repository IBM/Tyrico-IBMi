#include <qsnddtaq.h>
#include <qrcvdtaq.h>
#include <string.h>
#include <sstream>
#include <ctime>
#include <string>
#include <stdlib.h>
#include <stdio.h>



#define COUNTER_LENGTH 7 // Number of bytes in counter
#define KEY_SIZE 17 // Length of the key

using namespace std;

typedef struct varchar1000 {
    unsigned short len;
    char data[1000];
} Varchar1000;

// Last time the counter was reset
time_t lastResetTime = time(0);

// A counter to help uniquely identify keys
int counter = 0;

char* int_to_string(int value){
    char* buffer = (char*)malloc(COUNTER_LENGTH + 1);  
    if (buffer == NULL) {
        printf("Memory allocation failed\n");
        exit;  // Return an error code or handle the error appropriately
    }
    if (buffer) {
        snprintf(buffer, (std::size_t)COUNTER_LENGTH + 1, "%d", value);
    }
    return buffer;
}

char* time_to_string(time_t value) {
    int max_length = 20;
    char* buffer = (char*)malloc(max_length + 1);  
    if (buffer == NULL) {
        printf("Memory allocation failed\n");
        exit;  // Return an error code or handle the error appropriately
    }
    if (buffer) {
        snprintf(buffer, (std::size_t)max_length + 1, "%ld", value);
    }
    return buffer;
}

// Returns a unique key
char* getKey() {
    char* keyBuffer = (char*)malloc(KEY_SIZE + 1);
    if (keyBuffer == NULL) {
        printf("Memory allocation failed\n");
    }
    time_t timestampSeconds = time(NULL);
    if (timestampSeconds - lastResetTime >= 1) {
        counter = 0;
        lastResetTime = timestampSeconds;
    } else {
        counter++;
    }
    
    char* timeStr = time_to_string(timestampSeconds);
    char* counterStr = int_to_string(counter);

    // Pad with leading zeros
    int numLen = strlen(counterStr);
    char padded_counter[COUNTER_LENGTH + 1];

    memset(padded_counter, '0', COUNTER_LENGTH - numLen);
    memcpy(padded_counter + COUNTER_LENGTH - numLen, counterStr, numLen);
    padded_counter[COUNTER_LENGTH] = '\0';
    
    snprintf(keyBuffer, (std::size_t)KEY_SIZE + 1, "%s%s", timeStr, padded_counter);
    free(timeStr);
    free(counterStr);
    return keyBuffer;
}

// Read a record from the data queue and write the result into the output buffer. Return 0 if successful
// and -1 if the output buffer passed in was not big enough
extern "C" int readDataQueue(char* dtaq_name, char* dtaq_lib, char* key, Varchar1000 *output){
    char _out[1024];
    memset(_out, '\0', 1024);
    decimal(5,0) returned_len = 0;
    char keybuf[KEY_SIZE] __attribute__((aligned(16)));
    memset(keybuf, ' ', KEY_SIZE);
    memcpy(keybuf, key, strlen(key));
    decimal(5,0) timeout = 1;
    _DecimalT<5,0> len = __D("0");

    QRCVDTAQ(
      // 1 	Data queue name 	Input 	Char(10)
      dtaq_name,
      // 2 	Library name 	Input 	Char(10)
      dtaq_lib,
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
      (decimal(3, 0))KEY_SIZE,
      // 8 	Key data 	I/O 	Char(*)
      (void *)keybuf,
      // 9 	Length of sender information 	Input 	Packed(3,0)
      (decimal(3, 0))0,
      // 10 	Sender information 	Output 	Char(*)
      0);

    if (returned_len == 0) {
        printf("QRCVDTAQ failed for key %s\n", key);
    }

    output->len = strlen(_out);
    if (output->len > sizeof(output->data)){
        return -1;
    }

    memcpy(output->data, _out, output->len);
    return 0;
}

// Writes a message to dtaq_name using QSNDDTAQ
int writeDataQueueInternal(char* dtaq_name, char* dtaq_lib, char* message){
        // printf("start writeDataQueueInternal\n");
        // _DecimalT<5,0> len = __D("0");
        int len = 0;
        // printf("len allocated\n");
        len += strlen(message);
        // printf("len incremented\n");
        printf("Sending message %s\n", message);
        // Call the QSNDDTAQ API

        errno = 0; // Reset errno before calling the API
        QSNDDTAQ(dtaq_name, dtaq_lib, len, (void *)message);
        // printf("finish writeDataQueueInternal\n");
        return errno;
}

// Writes a message to dtaq_name. Returns the generated key if the write was successful and returns
// empty string otherwise.
extern "C" void writeDataQueue(char* dtaq_name, char* dtaq_lib, char* value, char* returned_key){
    // printf("start writeDataQueue\n");
    char* key = getKey();
    int key_len = strlen(key);
    std::size_t message_length = key_len + strlen(value) + 1; // +1 for null terminator
    char message[message_length]; // Buffer for the message

    // Create the message
    snprintf(message, sizeof(message), "%s%s", key, value);
    int error = writeDataQueueInternal(dtaq_name, dtaq_lib, message);
    if (error != 0){
        printf("Failed to send message to %s\n",
            message);
    }
    
    memcpy(returned_key, key, key_len + 1);
    free(key);
    // printf("finish writeDataQueue\n");
}


// #define DTAQ_IN  "D16       "         // Data queue name
// #define DTAQ_OUT "D18KEYED  "       // Data queue name
// #define DTAQ_LIB "JONATHAN  "    // Data queue library

// using namespace std;

// string flowers[] = {
// "[7.47,2.47,5.34,2.34]",
// "[5.12,2.28,6.81,0.39]",
// "[7.16,3.70,4.58,1.24]",
// "[4.99,2.92,4.22,2.23]",
// "[6.09,4.31,2.46,1.96]",
// "[6.22,4.29,6.53,1.46]",
// "[5.54,2.13,2.74,0.43]",
// "[7.46,3.85,4.70,1.51]",
// "[7.76,3.43,6.54,0.64]",
// "[7.68,3.65,6.63,2.35]",
// "[7.03,2.31,6.68,0.78]",
// "[6.44,2.68,5.40,2.13]",
// "[5.97,2.56,6.14,0.56]",
// "[4.56,2.60,3.33,0.79]",
// "[4.94,2.94,6.77,0.68]",
// "[7.85,2.82,4.00,1.28]",
// "[4.66,4.10,2.66,2.31]",
// "[7.43,2.85,3.06,0.56]",
// "[5.29,2.62,2.28,0.32]",
// "[4.70,2.54,1.29,2.42]",
// "[5.92,4.22,3.32,0.81]",
// "[6.64,3.18,2.29,1.13]",
// "[5.25,2.10,3.07,0.47]",
// "[6.00,2.44,1.75,2.43]",
// "[6.10,2.44,5.30,0.19]",
// "[7.13,4.25,5.67,0.74]",
// "[6.80,2.37,5.09,0.37]",
// "[7.76,2.48,2.66,0.63]"};

// int data_length = sizeof(flowers) / sizeof(flowers[0]);


// int main() {
//     printf("hello");
//     const int num_messages = 100000;
//     string keys[num_messages];
//     int buffer_size = 100;

//     for (int i = 0; i < num_messages; i++) {
//         char* message = (char*)(flowers[i % data_length]).c_str();
//         char key[buffer_size];
//         printf("sending message %d\n", i);

//         writeDataQueue(DTAQ_IN, DTAQ_LIB, message, key);
//         // Add key to the array for later use
//         keys[i] = string(key);
//         // if (i % 100000 == 0){
//         //     free(keys);

//         // }
//     }

//     // for (int i = 0; i < num_messages; i++){
//     //     string key = keys[i];
//     //     char res[buffer_size];
//     //     readDataQueue(DTAQ_OUT, DTAQ_LIB, (char*)key.c_str(), res);
//     //     printf("Received message key %s, value %s\n", key.c_str(), res);
//     // }
    
//     return 0;
// }
