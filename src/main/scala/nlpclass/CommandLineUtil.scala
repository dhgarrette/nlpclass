package nlpclass

object CommandLineUtil {
  private[this] val OptionRegex = "--(.*)".r

  def parseArgs(args: Array[String]) = {
    val parsedArgs =
      ("" +: args.toVector) // prepend an empty arg so sliding will work
        .sliding(2).flatMap {
          case Seq(OptionRegex(option), argument) => Some(option, argument) // if the first thing is an option
          case Seq(_, OptionRegex(_)) => None // if the second thing is an option
          case Seq(_, argument) => Some("argument", argument) // if no options are involved, then it's a normal argument
          case Seq(_) => None
        }

    val (argumentList, optionList) =
      parsedArgs.partition { // separate normal arguments from options
        case ("argument", argument) => true
        case _ => false
      }

    val arguments = argumentList.map(_._2).toVector // arguments are a Vector
    val options = optionList.toMap // options are a Map
    (arguments, options)
  }

}
