BTW TESTCASES.ZIP-----------------------------------------------
BTW PART 1 - OPS FOLDER-----------------------------------------------

HAI
BTW ARITHOP.LOL-----------------------------------------------
BTW for arithmetic operations
  OBTW
    if your interpreter does not implement IT,
    move the expressions to the VISIBLE statement
  TLDR

  BTW basic expressions
  SUM OF 1 AN 2
  VISIBLE IT
  DIFF OF 1 AN 2
  VISIBLE IT
  PRODUKT OF 1 AN 2
  VISIBLE IT
  QUOSHUNT OF 1.0 AN 2
  VISIBLE IT
  MOD OF 1 AN 2
  VISIBLE IT
  BIGGR OF 1 AN 2
  VISIBLE IT
  SMALLR OF 1 AN 2
  VISIBLE IT

  BTW compound expressions
  SUM OF PRODUKT OF 3 AN 5 AN BIGGR OF DIFF OF 17 AN 2 AN 5
  VISIBLE IT
  BIGGR OF PRODUKT OF 11 AN 2 AN QUOSHUNT OF SUM OF 3 AN 5 AN 2
  VISIBLE IT

OBTW 
  arithop.lol originally has var1 and var2 varidents, but I changed them so 
  as to avoid conflicts with other files that use the same varidents 
TLDR

  BTW arithmetic with variables
  I HAS A x1 ITZ 5
  I HAS A y1 ITZ 3
  
  DIFF OF y1 AN x1
  VISIBLE IT
  MOD OF y1 AN x1
  VISIBLE IT
  BIGGR OF SUM OF y1 AN x1 AN PRODUKT OF x1 AN y1
  VISIBLE IT
  SUM OF x1 AN 12.0
  VISIBLE IT
  

BTW ASSIGNOP.LOL-----------------------------------------------
BTW for assignment to variables
  I HAS A var1
  I HAS A var2
  I HAS A var3
  I HAS A var4
  I HAS A var5
  I HAS A var6
  I HAS A var7
  I HAS A var8
  
  BTW assignment of literals
  var1 R 17
  var2 R "seventeen"
  var3 R FAIL
  var4 R 2.18

  BTW printing...
  VISIBLE var1
  VISIBLE var2
  VISIBLE var3
  VISIBLE var4

  BTW assignment of expressions
  var5 R PRODUKT OF 1 AN 7
  var6 R WON OF WIN AN FAIL
  var7 R BOTH SAEM var1 AN var2
  var8 R EITHER OF FAIL AN FAIL

  BTW IT!!!!
  IT R "am IT"

  BTW printing...
  VISIBLE var5
  VISIBLE var6
  VISIBLE var7
  VISIBLE var8
  VISIBLE IT


BTW BOOLOP.LOL-----------------------------------------------
BTW for boolean and comparison operations
  BTW basic expressions
  BOTH OF WIN AN FAIL
  VISIBLE IT
  BOTH OF FAIL AN FAIL
  VISIBLE IT
  EITHER OF FAIL AN FAIL
  VISIBLE IT
  WON OF FAIL AN WIN
  VISIBLE IT
  NOT WIN
  VISIBLE IT
  ALL OF WIN AN WIN AN WIN AN FAIL AN WIN MKAY
  VISIBLE IT
  ANY OF WIN AN WIN AN WIN AN FAIL AN WIN MKAY
  VISIBLE IT

  BTW compound expressions
  BOTH OF NOT WIN AN NOT WIN
  VISIBLE IT
  EITHER OF NOT WIN AN WIN
  VISIBLE IT
  WON OF BOTH OF WIN AN WIN AN EITHER OF WIN AN FAIL
  VISIBLE IT
  ALL OF WIN AN BOTH OF WIN AN NOT FAIL AN WIN AN WON OF WIN AN NOT WIN MKAY
  VISIBLE IT

OBTW 
  boolop.lol originally has var1 and var2 varidents, but I changed them so 
  as to avoid conflicts with other files that use the same varidents 
TLDR

  BTW with variables
  I HAS A x2 ITZ WIN
  I HAS A y2 ITZ FAIL
  
  NOT x2
  VISIBLE IT
  ALL OF x2 AN WIN AN WIN AN y2 AN WIN MKAY
  VISIBLE IT


BTW COMPOP.LOL-----------------------------------------------
BTW for comparison operations
  BTW basic expressions
  BOTH SAEM 1 AN 1
  VISIBLE IT
  BOTH SAEM 1 AN 2
  VISIBLE IT
  BOTH SAEM 2 AN 2.0
  VISIBLE IT
  DIFFRINT 3 AN 4
  VISIBLE IT
  DIFFRINT 4 AN 4
  VISIBLE IT

  BTW compound expressions
  DIFFRINT 2 AN BIGGR OF 1 AN 2
  VISIBLE IT
  DIFFRINT BIGGR OF 1 AN 2 AN SMALLR OF 3 AN 2
  VISIBLE IT
  DIFFRINT BOTH SAEM 1 AN 2 AN DIFFRINT 1 AN 2
  VISIBLE IT

OBTW 
  compop.lol originally has var1 and var2 varidents, but I changed them so 
  as to avoid conflicts with other files that use the same varidents 
TLDR

  BTW with variables
  I HAS A x3 ITZ WIN
  I HAS A y3 ITZ FAIL

  DIFFRINT x3 AN y3
  VISIBLE IT
  BOTH SAEM x3 AN y3
  VISIBLE IT
  DIFFRINT BOTH SAEM x3 AN y3 AN DIFFRINT x3 AN x3
  VISIBLE IT


KTHXBYE