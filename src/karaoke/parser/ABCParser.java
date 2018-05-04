package karaoke.parser;

import java.awt.Label;
import java.beans.Expression;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;
//import edu.mit.eecs.parserlib.Visualizer;
import karaoke.ABC;

public class ABCParser {
    /**
     * Main method. Parses and then reprints an example expression.
     * 
     * @param args command line arguments, not used
     * @throws UnableToParseException if example expression can't be parsed
     */
    public static void main(final String[] args) throws UnableToParseException {
        final String input = "foo_bar.png|baz-qux.jpg";
        System.out.println(input);
        final ABC abc = ABCParser.parse(input);
        System.out.println(abc);
    }
    
    // the nonterminals of the grammar
    private static enum ABCGrammar {
        ABC, ABC_HEADER, FIELD_NUMBER, FIELD_TITLE, OTHER_FIELDS, 
        FIELD_COMPOSER, FIELD_DEFAULT_LENGTH, FIELD_METER, FIELD_TEMPO, 
        FIELD_VOICE, FIELD_KEY, KEY, KEYNOTE, KEY_ACCIDENTAL, MADE_MINOR, 
        METER, METER_FRACTION, TEMPO, ABC_BODY, ABC_LINE, ELEMENT, 
        NOTE_ELEMENT, NOTE, PITCH, OCTAVE, NOTE_LENGTH, NOTE_LENGTH_STRICT, 
        ACCIDENTAL, BASENOTE, REST_ELEMENT, TUPLET_ELEMENT, TUPLET_SPEC, 
        CHORD, BARLINE, NTH_REPEAT, LYRIC, LYRICAL_ELEMENT, LYRIC_TEXT, 
        COMMENT, COMMENT_TEXT, END_OF_LINE, TEXT, WORD, DIGIT, NEWLINE, 
        SPACE_OR_TAB,
    }
    

    private static Parser<ABCGrammar> parser = makeParser();
    
    /**
     * Compile the grammar into a parser.
     * 
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has syntax errors
     */
    private static Parser<ABCGrammar> makeParser() {
        try {
            // read the grammar as a file, relative to the project root.
            final File grammarFile = new File("src/karaoke.parser/Abc.g");
            return Parser.compile(grammarFile, ABCGrammar.ABC);

        // Parser.compile() throws two checked exceptions.
        // Translate these checked exceptions into unchecked RuntimeExceptions,
        // because these failures indicate internal bugs rather than client errors
        } catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("the grammar has a syntax error", e);
        }
    }

    /**
     * Parse a string into an expression.
     * @param string string to parse
     * @return Expression parsed from the string
     * @throws UnableToParseException if the string doesn't match the Expression grammar
     */
    public static ABC parse(final String string) throws UnableToParseException {
        // parse the example into a parse tree
        final ParseTree<ABCGrammar> parseTree = parser.parse(string);

        // display the parse tree in various ways, for debugging only
        // System.out.println("parse tree " + parseTree);
        // Visualizer.showInBrowser(parseTree);

        // make an AST from the parse tree
        final ABC abc = makeAbstractSyntaxTree(parseTree);
        // System.out.println("AST " + abc);
        
        return abc;
    }
    
    /**
     * Convert a parse tree into an abstract syntax tree.
     * 
     * @param parseTree constructed according to the grammar in Exression.g
     * @return abstract syntax tree corresponding to parseTree
     */
    private static ABC makeAbstractSyntaxTree(final ParseTree<ABCGrammar> parseTree) {
        switch (parseTree.name()) {
         
        
        case ABC: // expression ::= topToBottom;
            {
                return makeAbstractSyntaxTree(parseTree.children().get(0));
            }
        
        case ABC_HEADER: // topToBottom ::= sideBySide (topToBottomOperator sideBySide)*;
            {
                System.out.println("hey");
//                final List<ParseTree<ABCGrammar>> children = parseTree.children();
//                Expression expression = makeAbstractSyntaxTree(children.get(0));
//                for(int i = 2; i < children.size(); i += 2)
//                    expression = new TopToBottom(expression, makeAbstractSyntaxTree(children.get(i)));
//                return expression;
            }    
        
        case FIELD_NUMBER: {
            System.out.println("hey");

        }
        case FIELD_TITLE: {
            System.out.println("hey");

        }
        case OTHER_FIELDS: {
            System.out.println("hey");

        }
        case FIELD_COMPOSER: {
            System.out.println("hey");

        }
        case FIELD_DEFAULT_LENGTH: {
            System.out.println("hey");

        }
        case FIELD_METER: {
            System.out.println("hey");

        }
        case FIELD_TEMPO:  {
            System.out.println("hey");

        }
        case FIELD_VOICE: {
            System.out.println("hey");

        }
        default:
            throw new AssertionError("should never get here");
        }

    }

}