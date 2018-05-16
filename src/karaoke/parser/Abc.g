// Grammar for ABC music notation 
// A subset of abc 2.1

abc ::= abc_header abc_body;

////////////////////////////////////////////////////
// Header
////////////////////////////////////////////////////

// ignore space_or_tab between terminals in the header
@skip space_or_tab {
    abc_header ::= field_number comment* field_title other_fields* field_key;
    
    field_number ::= "X:" number end_of_line;
    field_title ::= "T:" text end_of_line;
    other_fields ::= field_composer | field_default_length | field_meter | field_tempo | field_voice | comment;
    field_composer ::= "C:" text end_of_line;
    field_default_length ::= "L:" note_length_strict end_of_line;
    field_meter ::= "M:" meter end_of_line;
    field_tempo ::= "Q:" tempo end_of_line;
    field_voice ::= "V:" text end_of_line;
    field_key ::= "K:" key end_of_line;
    
    key ::= keynote mode_minor?;
    keynote ::= basenote key_accidental?;
    key_accidental ::= "#" | "b";
    mode_minor ::= "m";
    
    meter ::= "C" | "C|" | meter_fraction;
    meter_fraction ::= numerator "/" denominator;
    
    tempo ::= meter_fraction "=" number;
}

////////////////////////////////////////////////////
// Body
////////////////////////////////////////////////////

// spaces and tabs have explicit meaning in the body, don't automatically ignore them

abc_body ::= abc_line+;
abc_line ::= element+ end_of_line (lyric end_of_line)? | middle_of_body_field | comment;

element ::= musical_element | barline | nth_repeat | space_or_tab;

musical_element ::= note_element | rest_element | tuplet_element;

// notes
note_element ::= note | chord;

note ::= pitch note_length?;
pitch ::= accidental? basenote octave?;
octave ::= "'"+ | ","+;
note_length ::= numerator ("/" denominator?)? | ("/" denominator?);
note_length_strict ::= numerator "/" denominator;
numerator ::= number;
denominator ::= number;

// "^" is sharp, "_" is flat, and "=" is neutral
accidental ::= "^" | "^^" | "_" | "__" | "=";

basenote ::= "C" | "D" | "E" | "F" | "G" | "A" | "B" | "c" | "d" | "e" | "f" | "g" | "a" | "b";

// rests
rest_element ::= "z" note_length?;

// tuplets
tuplet_element ::= tuplet_spec note_element+;
tuplet_spec ::= "(" number;

// chords
chord ::= "[" note+ "]";

barline ::= "|" | "||" | "[|" | "|]" | ":|" | "|:";
nth_repeat ::= "[1" | "[2";

// A voice field might reappear in the middle of a piece
// to indicate the change of a voice
middle_of_body_field ::= field_voice;

lyric ::= "w:" lyrical_element*;
lyrical_element ::= " "+ | "-" | "_" | "*" | "|" | lyric_text;
// lyric_text should be defined appropriately
lyric_text ::= ("\\-" | [^ -_*|%\n\r])+;

////////////////////////////////////////////////////
// General
////////////////////////////////////////////////////

comment ::= space_or_tab* "%" comment_text newline;
// comment_text should be defined appropriately;
comment_text ::= [^\n\r]*;

end_of_line ::= comment | newline;

text ::= [^%\n\r]*;
number ::= digit+;
digit ::= [0-9];
newline ::= "\n" | "\r" "\n"?;
space_or_tab ::= " " | "\t";
