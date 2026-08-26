import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogTrigger,
} from '#/components/ui/alert-dialog'
import { Button } from '#/components/ui/button'

interface ConfirmButtonProps {
    label: string
    title: string
    description: string
    confirmLabel?: string
    variant?: 'default' | 'outline' | 'destructive'
    disabled?: boolean
    onConfirm: () => void
}

// A button that requires an explicit "are you sure" step before firing -- for any
// action that's hard to reverse (deleting, cancelling something live). Not every
// action needs this: reserve it for the ones with a real "oops" consequence.
export function ConfirmButton({
    label,
    title,
    description,
    confirmLabel = 'Confirm',
    variant = 'outline',
    disabled,
    onConfirm,
}: ConfirmButtonProps) {
    return (
        <AlertDialog>
            <AlertDialogTrigger asChild>
                <Button type="button" variant={variant} disabled={disabled}>
                    {label}
                </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle>{title}</AlertDialogTitle>
                    <AlertDialogDescription>{description}</AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>Go back</AlertDialogCancel>
                    <AlertDialogAction
                        variant={variant === 'destructive' ? 'destructive' : 'default'}
                        onClick={onConfirm}
                    >
                        {confirmLabel}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    )
}
