#include <qsnddtaq.h>
#include <qrcvdtaq.h>
#include <string.h>
#include <sstream>
#include <ctime>


#define COUNTER_LENGTH 7 // Number of bytes in counter
#define KEY_SIZE 17 // Length of the key

using namespace std;

// Last time the counter was reset
time_t lastResetTime = time(0);

// A counter to help uniquely identify keys
int counter = 0;

// Returns the string representation of an object
template <typename T>
string to_string_custom(T value) {
    ostringstream oss;
    oss << value;
    return oss.str();
}

// Returns a unique key
string getKey(){
    // Gets the key, and modifies the counter and lastResetTime
    time_t timestampSeconds = time(0);
    if (timestampSeconds  - lastResetTime >= 1) {
        counter = 0;
        lastResetTime = timestampSeconds;
    } else {
        counter = counter + 1;
    }

    // Convert time_t to string. This is 10 characters long for the next two centuries.
    string timeStr = to_string_custom(timestampSeconds);
    
    // Convert int to string
    string numStr = to_string_custom(counter);

    // Pad with leading zeros to ensure 7 characters
    if (numStr.length() < COUNTER_LENGTH) {
        numStr.insert(0, COUNTER_LENGTH - numStr.length(), '0');
    }

    // Concatenate the strings. This is always 17 characters long.
    string key = timeStr + numStr;
    return key;
}

// Read a record from the data queue and return the result. Return empty string if the record
// was not found.
string readDataQueue(char* dtaq_name, char* library_name, string key){
    char _out[1024];
    decimal(5,0) returned_len = 0;
    char keybuf[KEY_SIZE] __attribute__((aligned(16)));
    memset(keybuf, ' ', KEY_SIZE);
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
      (decimal(3, 0))KEY_SIZE,
      // 8 	Key data 	I/O 	Char(*)
      (void *)keybuf,
      // 9 	Length of sender information 	Input 	Packed(3,0)
      (decimal(3, 0))0,
      // 10 	Sender information 	Output 	Char(*)
      0);

    if (returned_len == 0) {
        printf("QRCVDTAQ failed for key %s\n", key.c_str());
        return "";
    }

    return string(_out);
}

// Writes a message to dtaq_name using QSNDDTAQ
int writeDataQueueInternal(char* dtaq_name, char* dtaq_lib, char* message){

        _DecimalT<5,0> len = __D("0");
        len += strlen(message);
        printf("Sending message %s\n", message);
        // Call the QSNDDTAQ API

        errno = 0; // Reset errno before calling the API
        QSNDDTAQ(dtaq_name, dtaq_lib, len, (void *)message);
        return errno;
}

// Writes a message to dtaq_name. Returns the generated key if the write was successful and returns
// empty string otherwise.
string writeDataQueue(char* dtaq_name, char* dtaq_lib, string value){
    string key = getKey();
    int message_length = key.length() + value.length() + 1; // +1 for null terminator
    char message[message_length]; // Buffer for the message

    // Create the message
    snprintf(message, sizeof(message), "%s%s", key.c_str(), value.c_str());
    int error = writeDataQueueInternal(dtaq_name, dtaq_lib, message);
    if (error != 0){
        printf("Failed to send message to %s\n",
            message);
        return "";
    }
    return key;
}
