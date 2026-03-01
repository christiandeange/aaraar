#include <stdio.h>

int tiny_add(int a, int b) {
  return a + b;
}

const char* hello_world() {
  return "Hello, World!";
}

void print_hello_world() {
  printf("%s\n", hello_world());
}
