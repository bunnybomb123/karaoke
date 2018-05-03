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
        EXPRESSION, TOPTOBOTTOM, SIDEBYSIDE, BOTTOMOVERLAY, TOPOVERLAY, RESIZE, PRIMITIVE,
        TOPTOBOTTOMOPERATOR, LABEL, FILENAME, NUMBER, WHITESPACE,
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
            return Parser.compile(grammarFile, ABCGrammar.EXPRESSION);

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
    private static Expression makeAbstractSyntaxTree(final ParseTree<ExpressionGrammar> parseTree) {
        switch (parseTree.name()) {
        case EXPRESSION: // expression ::= topToBottom;
            {
                return makeAbstractSyntaxTree(parseTree.children().get(0));
            }
        
        case TOPTOBOTTOM: // topToBottom ::= sideBySide (topToBottomOperator sideBySide)*;
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                Expression expression = makeAbstractSyntaxTree(children.get(0));
                for(int i = 2; i < children.size(); i += 2)
                    expression = new TopToBottom(expression, makeAbstractSyntaxTree(children.get(i)));
                return expression;
            }    
            
        case SIDEBYSIDE: // sideBySide ::= bottomOverlay ('|' bottomOverlay)*;
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                Expression expression = makeAbstractSyntaxTree(children.get(0));
                for(int i = 1; i < children.size(); ++i)
                    expression = new SideBySide(expression, makeAbstractSyntaxTree(children.get(i)));
                return expression;
            }    
            
        case BOTTOMOVERLAY: // bottomOverlay ::= topOverlay ('_' topOverlay)*;
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                Expression expression = makeAbstractSyntaxTree(children.get(0));
                for(int i = 1; i < children.size(); ++i)
                    expression = new BottomOverlay(expression, makeAbstractSyntaxTree(children.get(i)));
                return expression;
            }    
            
        case TOPOVERLAY: // topOverlay ::= resize ('^' resize)*;
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                Expression expression = makeAbstractSyntaxTree(children.get(0));
                for(int i = 1; i < children.size(); ++i)
                    expression = new TopOverlay(expression, makeAbstractSyntaxTree(children.get(i)));
                return expression;
            }
        
        case RESIZE: // resize ::= primitive ('@' number 'x' number)*;
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                Expression expression = makeAbstractSyntaxTree(children.get(0));
                for(int i = 1; i < children.size(); i += 2)
                    expression = new Resize(expression,
                            Integer.parseInt(children.get(i).text()),
                            Integer.parseInt(children.get(i + 1).text()));
                return expression;
            }
            
        case PRIMITIVE: // primitive ::= filename | label | '(' expression ')';
            {
                return makeAbstractSyntaxTree(parseTree.children().get(0));
            }
            
        case LABEL: // label ::= '"' [^"]* '"';
            {
                String labelWithQuotes = parseTree.text();
                return new Label(labelWithQuotes.substring(1, labelWithQuotes.length() - 1));
            }
            
        case FILENAME: // filename ::= [A-Za-z0-9./][A-Za-z0-9./_-]*;
            {
                return new Filename(parseTree.text());
            }
        
        default:
            throw new AssertionError("should never get here");
        }

    }

}