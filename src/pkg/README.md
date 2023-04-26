CYK Parser created by Alexander Crowe and Gregory Vincent.
In order to use this parser, your included text file should follow some basic rules:
1. First and foremost, your CFG should be in CNF. The CYK Parser won't work otherwise. 
2. Second, there should be logical progression in your CFG and the input string you are passing.
So for example, consider this grammar below:

              S : AC|BC|epsilon
              A : a|b|BC|AC|epsilon 
              B : CA|CB|a|b
              C : a|b|S|DE|epsilon
              D : a
              E : b
              
Every single non-terminal follows the rules of CNF. Every terminal can be derived from a non-terminal.
Following this same pattern is your input string. Your input string should fit the language you want to derive it from.
So a string like "aba" would work for the grammar above. A word w = " aa " works for the above CFG, while something like "vv" clearly does not. 

Be sure to change the path variable in the App.java file (line 65) to the correct path relative to the file system on your machine.
