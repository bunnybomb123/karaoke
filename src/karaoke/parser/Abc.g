// Grammar for ABC music notation 
; A subset of abc 2.1 in BNF format

abc ::= ::= abc-header abc-body

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Header

; ignore space-or-tab between terminals in the header

@skip space-or-tab {
	abc-header ::= field-number comment* field-title other-fields* field-key
	
	field-number ::= "X:" digit+ end-of-line
	field-title ::= "T:" text end-of-line
	other-fields ::= field-composer | field-default-length | field-meter | field-tempo | field-voice | comment
	field-composer ::= "C:" text end-of-line
	field-default-length ::= "L:" note-length-strict end-of-line
	field-meter ::= "M:" meter end-of-line
	field-tempo ::= "Q:" tempo end-of-line
	field-voice ::= "V:" text end-of-line
	field-key ::= "K:" key end-of-line
	
	key ::= keynote mode-minor?
	keynote ::= basenote key-accidental?
	key-accidental ::= "#" | "b"
	mode-minor ::= "m"
	
	meter ::= "C" | "C|" | meter-fraction
	meter-fraction ::= digit+ "/" digit+
	
	tempo ::= meter-fraction "=" digit+
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Body

; spaces and tabs have explicit meaning in the body, don't automatically ignore them

abc-body ::= abc-line+
abc-line ::= element+ end-of-line (lyric end-of-line)?  | middle-of-body-field | comment
element ::= note-element | rest-element | tuplet-element | barline | nth-repeat | space-or-tab 

;; notes
note-element ::= note | chord

note ::= pitch note-length?
pitch ::= accidental? basenote octave?
octave ::= "'"+ | ","+
note-length ::= (digit+)? ("/" (digit+)?)?
note-length-strict ::= digit+ "/" digit+

;; "^" is sharp, "_" is flat, and "=" is neutral
accidental ::= "^" | "^^" | "_" | "__" | "="

basenote ::= "C" | "D" | "E" | "F" | "G" | "A" | "B"
        | "c" | "d" | "e" | "f" | "g" | "a" | "b"

;; rests
rest-element ::= "z" note-length?

;; tuplets
tuplet-element ::= tuplet-spec note-element+
tuplet-spec ::= "(" digit 

;; chords
chord ::= "[" note+ "]"

barline ::= "|" | "||" | "[|" | "|]" | ":|" | "|:"
nth-repeat ::= "[1" | "[2"

; A voice field might reappear in the middle of a piece
; to indicate the change of a voice
middle-of-body-field ::= field-voice

lyric ::= "w:" lyrical-element*
lyrical-element ::= " "+ | "-" | "_" | "*" | "|" | lyric-text
; lyric-text should be defined appropriately
lyric-text ::= [^ -_*|]+

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; General

comment ::= space-or-tab* "%" comment-text newline
; comment-text should be defined appropriately
comment-text ::= .*

end-of-line ::= comment | newline

digit ::= [0-9]
newline ::= "\n" | "\r" "\n"?
space-or-tab ::= " " | "\t"