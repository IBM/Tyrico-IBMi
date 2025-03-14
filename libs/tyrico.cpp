#include <qsnddtaq.h>
#include <qrcvdtaq.h>
#include <string.h>
#include <sstream>
#include <ctime>
#include <string>
#include <stdlib.h>
#include <stdio.h>
#include "tyrico.h"

#define COUNTER_LENGTH 7 // Number of bytes in counter
#define KEY_SIZE 17 // Length of the key
#define DTAQ_BUF_SIZE 10 // Standard buffer size for strings to be passed into the qsnddtaq 
                         // and qrcvdtaq apis

using namespace std;

// Last time the counter was reset
time_t last_reset_time = time(0);

// A counter to help uniquely identify keys
int counter = 0;

void int_to_string(int value, char* buffer, std::size_t buf_size){
        snprintf(buffer, buf_size, "%d", value);
}

void time_to_string(time_t value, char* buffer, std::size_t buf_size) {
        snprintf(buffer, (std::size_t)buf_size, "%ld", value);
}

void copyToSpacePaddedBuffer(char* dst, char* src, std::size_t buf_size){
    for (int i = 0; i < buf_size && src[i] != '\0'; i++) {
        dst[i] = src[i];
    }
}
// Generate a unique key, and copy it into key_buffer
void getKey(char* key_buffer, std::size_t buf_size) {
    // Reset the counter every second
    time_t timestamp_seconds = time(NULL);
    if (timestamp_seconds - last_reset_time >= 1) {
        counter = 0;
        last_reset_time = timestamp_seconds;
    } else {
        counter++;
    }

    int max_timestamp_length = 20;
    char counter_str[COUNTER_LENGTH + 1];
    char time_str[max_timestamp_length + 1];

    time_to_string(timestamp_seconds, time_str, max_timestamp_length + 1);
    int_to_string(counter, counter_str, COUNTER_LENGTH + 1);

    // Pad with leading zeros
    int numLen = strlen(counter_str);
    char padded_counter[COUNTER_LENGTH + 1];
    memset(padded_counter, '0', COUNTER_LENGTH - numLen);

    memcpy(padded_counter + COUNTER_LENGTH - numLen, counter_str, numLen);
    padded_counter[COUNTER_LENGTH] = '\0';
    
    snprintf(key_buffer, buf_size, "%s%s", time_str, padded_counter);
}

// Read a record from the data queue and write the result into the output buffer. Return 0 if successful
// and -1 if the output buffer passed in was not big enough.
extern "C" int readDataQueue(char* dtaq_name, char* dtaq_lib, char* key, Varchar1000 *output){

    // These buffers need to be exactly size 10 for QSNDDTAQ api
    char dtaq_name_buf[DTAQ_BUF_SIZE];
    char dtaq_lib_buf[DTAQ_BUF_SIZE];

    // The trailing characters need to be spaces
    memset(dtaq_name_buf, ' ', DTAQ_BUF_SIZE);
    memset(dtaq_lib_buf, ' ', DTAQ_BUF_SIZE);

    copyToSpacePaddedBuffer(dtaq_name_buf, dtaq_name, DTAQ_BUF_SIZE);
    copyToSpacePaddedBuffer(dtaq_lib_buf, dtaq_lib, DTAQ_BUF_SIZE);

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
      dtaq_name_buf,
      // 2 	Library name 	Input 	Char(10)
      dtaq_lib_buf,
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

// Writes a message to dtaq_name using QSNDDTAQ. Return 0 if successful, otherwise return the errno set by 
// QSNDDTAQ.
int writeDataQueueInternal(char* dtaq_name, char* dtaq_lib, char* message){
        int len = 0;
        len += strlen(message);
        printf("Sending message %s\n", message);

        errno = 0; // Reset errno before calling the API
        QSNDDTAQ(dtaq_name, dtaq_lib, len, (void *)message);
        return errno;
}


// Writes a message to dtaq_name. Sets returned_key to the generated key if the write was successful. 
// Return 0 if successful, otherwise return the error set by QSNDDTAQ.
extern "C" int writeDataQueue(char* dtaq_name, char* dtaq_lib, char* value, Key* returned_key){
    // These buffers need to be exactly size 10 for QSNDDTAQ api
    char dtaq_name_buf[DTAQ_BUF_SIZE];
    char dtaq_lib_buf[DTAQ_BUF_SIZE];

    // The trailing characters need to be spaces
    memset(dtaq_name_buf, ' ', DTAQ_BUF_SIZE);
    memset(dtaq_lib_buf, ' ', DTAQ_BUF_SIZE);

    copyToSpacePaddedBuffer(dtaq_name_buf, dtaq_name, DTAQ_BUF_SIZE);
    copyToSpacePaddedBuffer(dtaq_lib_buf, dtaq_lib, DTAQ_BUF_SIZE);

    char key_buffer[KEY_SIZE + 1];
    getKey(key_buffer, KEY_SIZE + 1);
    std::size_t message_length = KEY_SIZE + 1 + strlen(value) + 1; // +1 for null terminator
    char message[message_length]; // Buffer for the message

    // Create the message
    snprintf(message, sizeof(message), "%s%s", key_buffer, value);
    int error = writeDataQueueInternal(dtaq_name_buf, dtaq_lib_buf, message);
    if (error != 0){
        printf("Failed to send message to %s\n",
            message);
        return error;
    }

    memcpy(returned_key->data, key_buffer, KEY_SIZE + 1);
    return 0;
}
