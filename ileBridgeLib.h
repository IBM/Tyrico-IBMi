#include <string>

using namespace std;

extern "C" void readDataQueue(char* dtaq_name, char* dtaq_lib, char* key, char* output);

extern "C" void writeDataQueue(char* dtaq_name, char* dtaq_lib, char* value, char* returned_key);
